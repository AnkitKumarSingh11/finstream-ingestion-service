package dev.byankit.finstream_data_ingestion;

import dev.byankit.finstream_data_ingestion.apis.FileMetadataController;
import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import dev.byankit.finstream_data_ingestion.exceptions.NotFoundException;
import dev.byankit.finstream_data_ingestion.services.FileMetadataService;
import dev.byankit.finstream_data_ingestion.services.ProcessingMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class FileMetadataControllerTest {

    private FileMetadataService fileMetadataService;
    private ProcessingMetadataService processingMetadataService;
    private FileMetadataController controller;

    @BeforeEach
    void setUp() {
        fileMetadataService = Mockito.mock(FileMetadataService.class);
        processingMetadataService = Mockito.mock(ProcessingMetadataService.class);
        controller = new FileMetadataController(fileMetadataService, processingMetadataService);
    }

    @Test
    void testGetAllFilesMetadata() {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId("f123");
        Mockito.when(fileMetadataService.getAllFilesMetadata()).thenReturn(List.of(metadata));

        ResponseEntity<List<FileMetadata>> response = controller.getFileMetadata();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetFileMetadataByFileId_Success() {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId("f123");
        Mockito.when(fileMetadataService.getFileMetadataByFileId("f123")).thenReturn(Optional.of(metadata));

        ResponseEntity<FileMetadata> response = controller.getFileMetadata("f123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("f123", response.getBody().getFileId());
    }

    @Test
    void testGetFileMetadataByFileId_NotFound() {
        Mockito.when(fileMetadataService.getFileMetadataByFileId("invalid")).thenReturn(Optional.empty());
        Mockito.when(fileMetadataService.getFileMetadataByDocumentId("invalid")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.getFileMetadata("invalid"));
    }

    @Test
    void testGetFileMetadataProcessingStatus_Success() {
        ProcessingMetadata procMeta = new ProcessingMetadata();
        procMeta.setFileId("f123");
        procMeta.setDocumentId("doc123");
        procMeta.setProcessingStatus(ProcessingStatus.UPLOADED);
        Mockito.when(processingMetadataService.getProcessingMetadataByFileId("f123")).thenReturn(Optional.of(procMeta));

        ResponseEntity<ProcessingMetadata> response = controller.getFileMetadataProcessingStatus("f123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProcessingStatus.UPLOADED, response.getBody().getProcessingStatus());
    }

    @Test
    void testUpdateFileMetadataProcessingStatus() {
        ProcessingMetadata request = new ProcessingMetadata();
        request.setProcessingStatus(ProcessingStatus.PROCESSED);

        ProcessingMetadata updated = new ProcessingMetadata();
        updated.setFileId("f123");
        updated.setProcessingStatus(ProcessingStatus.PROCESSED);
        Mockito.when(processingMetadataService.updateProcessingStatus(eq("f123"), eq(ProcessingStatus.PROCESSED))).thenReturn(updated);

        ResponseEntity<ProcessingMetadata> response = controller.updateFileMetadata("f123", request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProcessingStatus.PROCESSED, response.getBody().getProcessingStatus());
    }
}
