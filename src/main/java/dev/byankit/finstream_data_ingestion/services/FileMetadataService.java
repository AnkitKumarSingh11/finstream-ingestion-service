package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;

import java.util.List;
import java.util.Optional;

public interface FileMetadataService {
    Optional<FileMetadata> getFileMetadataByFileId(String fileId);
    Optional<FileMetadata> getFileMetadataByDocumentId(String documentId);
    List<FileMetadata> getAllFilesMetadata();
    FileMetadata saveFileMetadata(FileMetadata fileMetadata);
}
