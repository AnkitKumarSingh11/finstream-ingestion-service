package dev.byankit.finstream_data_ingestion;

import dev.byankit.finstream_data_ingestion.config.HdfsProperties;
import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import dev.byankit.finstream_data_ingestion.events.FileUploadEvent;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import dev.byankit.finstream_data_ingestion.services.FileMetadataService;
import dev.byankit.finstream_data_ingestion.services.HDFSUploadServiceImpl;
import dev.byankit.finstream_data_ingestion.services.ProcessingMetadataService;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class HDFCUploadServiceImplTest {

    private FileMetadataService metadataService;
    private FileSystem hdfsFileSystem;
    private ProcessingMetadataService processingMetadataService;
    private HdfsProperties hdfsProperties;
    private ApplicationEventPublisher eventPublisher;
    private HDFSUploadServiceImpl uploadService;

    @BeforeEach
    void setUp() {
        metadataService = Mockito.mock(FileMetadataService.class);
        hdfsFileSystem = Mockito.mock(FileSystem.class);
        processingMetadataService = Mockito.mock(ProcessingMetadataService.class);
        hdfsProperties = new HdfsProperties();
        hdfsProperties.getUpload().setPath("/test/hdfs/path");
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        uploadService = new HDFSUploadServiceImpl(
                metadataService,
                hdfsFileSystem,
                processingMetadataService,
                hdfsProperties,
                eventPublisher
        );
    }

    @Test
    void testUploadFile_WritesFileCreatesMetadataAndPublishesEvent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv; charset=UTF-8",
                "header1,header2\nval1,val2".getBytes()
        );
        FileUploadRequest request = new FileUploadRequest(FileSource.hdfc, FileSourceType.bankstatement);

        FSDataOutputStream mockOutputStream = Mockito.mock(FSDataOutputStream.class);
        Mockito.when(hdfsFileSystem.create(any(Path.class))).thenReturn(mockOutputStream);

        Mockito.when(metadataService.saveFileMetadata(any(FileMetadata.class))).thenAnswer(invocation -> {
            FileMetadata fm = invocation.getArgument(0);
            fm.setDocumentId("doc-12345");
            return fm;
        });

        FileMetadata result = uploadService.uploadFile(file, request);

        assertNotNull(result);
        assertEquals("doc-12345", result.getDocumentId());
        assertTrue(result.getFilePath().startsWith("/test/hdfs/path/hdfc/bankstatement/csv/"), 
                "FilePath should contain full HDFS path instead of root upload path");

        // Verify ProcessingMetadata was created with fileId & documentId
        ArgumentCaptor<ProcessingMetadata> procCaptor = ArgumentCaptor.forClass(ProcessingMetadata.class);
        Mockito.verify(processingMetadataService).saveProcessingMetadata(procCaptor.capture());

        ProcessingMetadata savedProcMeta = procCaptor.getValue();
        assertEquals("doc-12345", savedProcMeta.getDocumentId());
        assertEquals(ProcessingStatus.UPLOADED, savedProcMeta.getProcessingStatus());
        assertNotNull(savedProcMeta.getStartDateTime());

        // Verify FileUploadEvent was published with full file path
        ArgumentCaptor<FileUploadEvent> eventCaptor = ArgumentCaptor.forClass(FileUploadEvent.class);
        Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertTrue(eventCaptor.getValue().filePath().startsWith("/test/hdfs/path/hdfc/bankstatement/csv/"));
    }

    @Test
    void testUploadFile_CleansUpOrphanedFileOnDbFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );
        FileUploadRequest request = new FileUploadRequest(FileSource.hdfc, FileSourceType.bankstatement);

        FSDataOutputStream mockOutputStream = Mockito.mock(FSDataOutputStream.class);
        Mockito.when(hdfsFileSystem.create(any(Path.class))).thenReturn(mockOutputStream);
        Mockito.when(hdfsFileSystem.exists(any(Path.class))).thenReturn(true);

        Mockito.when(metadataService.saveFileMetadata(any(FileMetadata.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> uploadService.uploadFile(file, request));

        // Verify orphan file cleanup was executed
        Mockito.verify(hdfsFileSystem).delete(any(Path.class), eq(false));
    }
}
