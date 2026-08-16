package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;

import java.util.List;
import java.util.Optional;

public interface ProcessingMetadataService {
    ProcessingMetadata saveProcessingMetadata(ProcessingMetadata processingMetadata);
    Optional<ProcessingMetadata> getProcessingMetadataByDocumentId(String documentId);
    Optional<ProcessingMetadata> getProcessingMetadataByFileId(String fileId);
    List<ProcessingMetadata> getProcessingMetadataByStatus(ProcessingStatus status);
    List<ProcessingMetadata> getAllProcessingMetadata();
    ProcessingMetadata updateProcessingStatus(String id, ProcessingStatus status);
}
