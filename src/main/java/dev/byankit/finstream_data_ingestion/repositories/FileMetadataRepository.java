package dev.byankit.finstream_data_ingestion.repositories;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    //  function to find the metadata by the fileId
    Optional<FileMetadata> findByFileId(String fileId);
}
