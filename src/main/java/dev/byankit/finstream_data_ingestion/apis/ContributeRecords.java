package dev.byankit.finstream_data_ingestion.apis;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import dev.byankit.finstream_data_ingestion.services.HDFCUploadServiceImpl;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads/")
public class ContributeRecords {
    private final HDFCUploadServiceImpl fileUploadService;

    @Autowired
    public ContributeRecords(final HDFCUploadServiceImpl fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/{file_source}/{file_type}/")
    public ResponseEntity<? extends FileMetadata> uploadFinanceStatementFile(
        @RequestParam("file") MultipartFile file,
        @PathVariable("file_source") FileSource fileSource,
        @PathVariable("file_type") FileSourceType fileType
    ) throws FileUploadException {
        FileUploadRequest request = new FileUploadRequest(fileSource, fileType);

        //  uploads the file to an HDFS location
        FileMetadata metadata = this.fileUploadService.uploadFile(file, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(metadata);
    }
}
