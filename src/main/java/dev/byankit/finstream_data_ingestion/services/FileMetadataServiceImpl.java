package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.repositories.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileMetadataServiceImpl(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public Optional<FileMetadata> getFileMetadataByFileId(String fileId) {
        Optional<FileMetadata> metadata = this.fileMetadataRepository.findByFileId(fileId);
        if (metadata.isEmpty()) {
            metadata = this.fileMetadataRepository.findById(fileId);
        }
        return metadata;
    }

    @Override
    public Optional<FileMetadata> getFileMetadataByDocumentId(String documentId) {
        Optional<FileMetadata> metadata = this.fileMetadataRepository.findById(documentId);
        if (metadata.isEmpty()) {
            metadata = this.fileMetadataRepository.findByFileId(documentId);
        }
        return metadata;
    }

    @Override
    public List<FileMetadata> getAllFilesMetadata() {
        return this.fileMetadataRepository.findAll();
    }

    @Override
    public FileMetadata saveFileMetadata(FileMetadata fileMetadata) {
        if (fileMetadata.getFileId() == null) {
            fileMetadata.setFileId(
                generateUUID().toString()
            );
        }

        return  this.fileMetadataRepository
                .save(fileMetadata);
    }

    private UUID generateUUID() {
        return UUID.randomUUID();
    }
}
