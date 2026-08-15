package dev.byankit.finstream_data_ingestion.apis;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.services.FileMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files/")
public class FileMetadataController {

    private final FileMetadataService fileMetadataService;

    @Autowired
    public FileMetadataController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @GetMapping("")
    public ResponseEntity<List<FileMetadata>> getFileMetadata() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                    this.fileMetadataService
                    .getAllFilesMetadata()
                );
    }

    @GetMapping("/{file_id}")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable Long file_id) {
        return null;
    }

    @GetMapping("/{file_id}/processing/status/")
    public ResponseEntity<List<FileMetadata>> getFileMetadataByProcessingStatus(@PathVariable Long file_id) {
        return null;
    }

    @PatchMapping("/{file_id}/processing/status/")
    public ResponseEntity<FileMetadata> updateFileMetadata(@PathVariable Long file_id, @RequestBody FileMetadata fileMetadata) {
        return null;
    }
}
