package dev.byankit.finstream_data_ingestion.repositories;

import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingMetadataRepository extends MongoRepository<ProcessingMetadata, String> {
    Optional<ProcessingMetadata> findByDocumentId(String documentId);
    Optional<ProcessingMetadata> findByFileId(String fileId);
    List<ProcessingMetadata> findByProcessingStatus(ProcessingStatus processingStatus);
}
