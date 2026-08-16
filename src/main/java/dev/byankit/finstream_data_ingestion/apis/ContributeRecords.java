package dev.byankit.finstream_data_ingestion.apis;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import dev.byankit.finstream_data_ingestion.services.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File Upload API", description = "Endpoints for uploading financial statement files to HDFS")
@RestController
@RequestMapping("/uploads/")
public class ContributeRecords {

    private static final Logger log = LoggerFactory.getLogger(ContributeRecords.class);

    private final FileUploadService fileUploadService;

    @Autowired
    public ContributeRecords(final FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Operation(summary = "Upload financial statement file", description = "Uploads a PDF, CSV, or JSON financial statement file to HDFS and creates initial metadata records.")
    @PostMapping(value = "/{file_source}/{file_type}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadata> uploadFinanceStatementFile(
        @Parameter(description = "Multipart file to upload") @RequestParam("file") MultipartFile file,
        @Parameter(description = "Source of the financial statement file (e.g. hdfc, sbi, paytm)") @PathVariable("file_source") FileSource fileSource,
        @Parameter(description = "Source type of the financial statement file (e.g. bankstatement, upiservice, wallet)") @PathVariable("file_type") FileSourceType fileType
    ) throws FileUploadException {
        log.info("Received upload request for source: {}, type: {}, filename: {}, size: {} bytes",
                fileSource, fileType, file.getOriginalFilename(), file.getSize());

        FileUploadRequest request = new FileUploadRequest(fileSource, fileType);
        FileMetadata metadata = this.fileUploadService.uploadFile(file, request);

        log.info("Upload completed successfully for fileId: {}", metadata.getFileId());
        return ResponseEntity.status(HttpStatus.OK).body(metadata);
    }
}
