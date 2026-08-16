package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.config.HdfsProperties;
import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.FileUploadType;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import dev.byankit.finstream_data_ingestion.events.FileUploadEvent;
import dev.byankit.finstream_data_ingestion.exceptions.BadRequestException;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Service
public class HDFSUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(HDFSUploadServiceImpl.class);

    private final FileMetadataService metadataService;
    private final FileSystem hdfsFileSystem;
    private final ProcessingMetadataService processingMetadataService;
    private final HdfsProperties hdfsProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public HDFSUploadServiceImpl(
            FileMetadataService metadataService,
            FileSystem hdfsFileSystem,
            ProcessingMetadataService processingMetadataService,
            HdfsProperties hdfsProperties,
            ApplicationEventPublisher eventPublisher
    ) {
        this.metadataService = metadataService;
        this.hdfsFileSystem = hdfsFileSystem;
        this.processingMetadataService = processingMetadataService;
        this.hdfsProperties = hdfsProperties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public FileMetadata uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) throws FileUploadException {
        validateFileType(file);
        validateFileUploadRequest(fileUploadRequest);

        if (file.getContentType() == null) {
            throw new BadRequestException("File content type is null");
        }

        String rawType = file.getContentType().split(";")[0].trim();
        String extension = rawType.contains("/") ? rawType.split("/")[1] : "bin";
        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + "." + extension;
        String uploadPath = hdfsProperties.getUpload().getPath();

        Path path = new Path(
            uploadPath + Path.SEPARATOR +
                    fileUploadRequest.fileSource() + Path.SEPARATOR +
                    fileUploadRequest.fileSourceType() + Path.SEPARATOR +
                    extension + Path.SEPARATOR +
                    fileName
        );

        log.info("Streaming uploaded file '{}' ({}) to HDFS path: {}", file.getOriginalFilename(), file.getSize(), path);

        try (
            InputStream inputStream = file.getInputStream();
            FSDataOutputStream outputStream = hdfsFileSystem.create(path);
        ) {
            inputStream.transferTo(outputStream);
            log.info("Successfully wrote stream to HDFS path: {}", path);
        } catch (IOException e) {
            log.error("Failed to write stream to HDFS path: {}", path, e);
            throw new RuntimeException("Failed to stream file to HDFS path: " + path, e);
        }

        FileMetadata fileMetadata;
        try {
            fileMetadata = writeMetadataToDb(
                fileUploadRequest,
                fileId,
                fileName,
                uploadPath,
                file.getContentType(),
                file.getSize()
            );

            saveFileProcessingMetadata(fileMetadata);

            // Publish async file upload event
            eventPublisher.publishEvent(new FileUploadEvent(
                fileMetadata.getFileId(),
                fileMetadata.getDocumentId(),
                fileUploadRequest.fileSource(),
                fileUploadRequest.fileSourceType(),
                uploadPath
            ));

            log.info("Published FileUploadEvent for fileId: {}", fileMetadata.getFileId());

        } catch (Exception e) {
            log.error("Database error after streaming to HDFS. Cleaning up orphaned file: {}", path, e);
            try {
                if (hdfsFileSystem.exists(path)) {
                    hdfsFileSystem.delete(path, false);
                    log.info("Successfully deleted orphaned file from HDFS: {}", path);
                }
            } catch (IOException ioException) {
                log.warn("Failed to delete orphaned file from HDFS: {}", path, ioException);
            }
            throw e;
        }

        return fileMetadata;
    }

    private ProcessingMetadata saveFileProcessingMetadata(FileMetadata fileMetadata) {
        ProcessingMetadata processingMetadata = new ProcessingMetadata();
        String docId = fileMetadata.getDocumentId() != null ? fileMetadata.getDocumentId() : fileMetadata.getFileId();
        processingMetadata.setDocumentId(docId);
        processingMetadata.setFileId(fileMetadata.getFileId());
        processingMetadata.setProcessingStatus(ProcessingStatus.UPLOADED);
        processingMetadata.setStartDateTime(Instant.now());
        return this.processingMetadataService.saveProcessingMetadata(processingMetadata);
    }

    private FileMetadata writeMetadataToDb(
        FileUploadRequest request,
        String fileId,
        String fileName,
        String hdfsUploadPath,
        String contentType,
        long fileSize
    ) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileId(fileId);
        metadata.setContentType(contentType);
        metadata.setFileSize(fileSize);
        metadata.setFilePath(hdfsUploadPath);
        metadata.setSource(request.fileSource());
        metadata.setSourceType(request.fileSourceType());

        return this.metadataService.saveFileMetadata(metadata);
    }

    private void validateFileType(MultipartFile file) throws FileUploadException {
        String rawContentType = file.getContentType();

        if (rawContentType == null) {
            throw new FileUploadException("File type is null");
        }

        String baseContentType = rawContentType.split(";")[0].trim().toLowerCase();

        FileUploadType uploadType = switch (baseContentType) {
            case "application/pdf" -> FileUploadType.PDF;
            case "application/csv", "text/csv", "application/x-csv", "text/comma-separated-values" -> FileUploadType.CSV;
            case "application/json", "text/json" -> FileUploadType.JSON;
            default -> null;
        };

        if (uploadType == null) {
            throw new BadRequestException("File type " + rawContentType + " is not accepted");
        }
    }

    private void validateFileUploadRequest(FileUploadRequest fileUploadRequest) {
        if (fileUploadRequest == null) {
            throw new BadRequestException("File upload request is null");
        }
    }
}
