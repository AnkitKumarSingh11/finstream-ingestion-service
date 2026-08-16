package dev.byankit.finstream_data_ingestion.apis;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import dev.byankit.finstream_data_ingestion.exceptions.BadRequestException;
import dev.byankit.finstream_data_ingestion.exceptions.NotFoundException;
import dev.byankit.finstream_data_ingestion.services.FileMetadataService;
import dev.byankit.finstream_data_ingestion.services.ProcessingMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "File Metadata & Processing API", description = "Endpoints for retrieving file metadata and updating processing statuses")
@RestController
@RequestMapping("/files")
public class FileMetadataController {

    private static final Logger log = LoggerFactory.getLogger(FileMetadataController.class);

    private final FileMetadataService fileMetadataService;
    private final ProcessingMetadataService processingMetadataService;

    @Autowired
    public FileMetadataController(FileMetadataService fileMetadataService, ProcessingMetadataService processingMetadataService) {
        this.fileMetadataService = fileMetadataService;
        this.processingMetadataService = processingMetadataService;
    }

    @Operation(summary = "Get all file metadata", description = "Returns a list of all file metadata records.")
    @GetMapping({"", "/"})
    public ResponseEntity<List<FileMetadata>> getFileMetadata() {
        log.debug("GET /files request received");
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.fileMetadataService.getAllFilesMetadata());
    }

    @Operation(summary = "Get file metadata by ID", description = "Fetches a single file metadata record matching the given file ID or document ID.")
    @GetMapping({"/{file_id}", "/{file_id}/"})
    public ResponseEntity<FileMetadata> getFileMetadata(
            @Parameter(description = "File UUID or MongoDB Document ID") @PathVariable("file_id") String fileId
    ) {
        log.debug("GET /files/{} request received", fileId);
        FileMetadata metadata = this.fileMetadataService.getFileMetadataByFileId(fileId)
                .or(() -> this.fileMetadataService.getFileMetadataByDocumentId(fileId))
                .orElseThrow(() -> new NotFoundException("File metadata not found for ID: " + fileId));
        return ResponseEntity.ok(metadata);
    }

    @Operation(summary = "Get processing status for a file", description = "Fetches the current processing metadata and status for the specified file ID.")
    @GetMapping({"/{file_id}/status", "/{file_id}/status/"})
    public ResponseEntity<ProcessingMetadata> getFileMetadataProcessingStatus(
            @Parameter(description = "File UUID or MongoDB Document ID") @PathVariable("file_id") String fileId
    ) {
        log.debug("GET /files/{}/status request received", fileId);

        ProcessingMetadata processingMetadata = this.processingMetadataService.getProcessingMetadataByFileId(fileId)
                .or(() -> this.processingMetadataService.getProcessingMetadataByDocumentId(fileId))
                .orElseThrow(() -> new NotFoundException("Processing metadata not found for ID: " + fileId));

        return ResponseEntity.ok(processingMetadata);
    }

    @Operation(summary = "Get files by processing status", description = "Returns a list of processing metadata records matching the given processing status (e.g. UPLOADED, PROCESSING, PROCESSED, PROCESSING_FAILED).")
    @GetMapping({"status/{status}", "status/{status}/"})
    public ResponseEntity<List<ProcessingMetadata>> getFileMetadataByProcessingStatus(
            @Parameter(description = "Processing status enum value") @PathVariable("status") ProcessingStatus status
    ) {
        log.debug("GET /files/status/{} request received", status);
        List<ProcessingMetadata> list = this.processingMetadataService.getProcessingMetadataByStatus(status);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Update file processing status", description = "Updates the processing status of a file. Used by external Spark / Airflow processing jobs.")
    @PatchMapping({"/{file_id}/status", "/{file_id}/status/"})
    public ResponseEntity<ProcessingMetadata> updateFileMetadata(
            @Parameter(description = "File UUID or MongoDB Document ID") @PathVariable("file_id") String fileId,
            @RequestBody ProcessingMetadata processingMetadataRequest
    ) {
        log.info("PATCH /files/{}/status request received", fileId);

        if (processingMetadataRequest == null || processingMetadataRequest.getProcessingStatus() == null) {
            throw new BadRequestException("Processing status in request body must not be null");
        }

        ProcessingStatus newStatus = processingMetadataRequest.getProcessingStatus();
        ProcessingMetadata updated = this.processingMetadataService.updateProcessingStatus(fileId, newStatus);
        return ResponseEntity.ok(updated);
    }
}
