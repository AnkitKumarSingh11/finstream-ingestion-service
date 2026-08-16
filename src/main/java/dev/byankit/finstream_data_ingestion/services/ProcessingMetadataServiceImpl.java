package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.ProcessingMetadata;
import dev.byankit.finstream_data_ingestion.enums.ProcessingStatus;
import dev.byankit.finstream_data_ingestion.exceptions.NotFoundException;
import dev.byankit.finstream_data_ingestion.repositories.ProcessingMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessingMetadataServiceImpl implements ProcessingMetadataService {

    private static final Logger log = LoggerFactory.getLogger(ProcessingMetadataServiceImpl.class);

    private final ProcessingMetadataRepository processingMetadataRepository;

    @Autowired
    public ProcessingMetadataServiceImpl(ProcessingMetadataRepository processingMetadataRepository) {
        this.processingMetadataRepository = processingMetadataRepository;
    }

    @Override
    public ProcessingMetadata saveProcessingMetadata(ProcessingMetadata processingMetadata) {
        log.info("Saving processing metadata for documentId: {}, fileId: {}, status: {}",
                processingMetadata.getDocumentId(), processingMetadata.getFileId(), processingMetadata.getProcessingStatus());
        return this.processingMetadataRepository.save(processingMetadata);
    }

    @Override
    public Optional<ProcessingMetadata> getProcessingMetadataByDocumentId(String documentId) {
        log.debug("Fetching processing metadata by documentId: {}", documentId);
        return this.processingMetadataRepository.findByDocumentId(documentId);
    }

    @Override
    public Optional<ProcessingMetadata> getProcessingMetadataByFileId(String fileId) {
        log.debug("Fetching processing metadata by fileId: {}", fileId);
        return this.processingMetadataRepository.findByFileId(fileId);
    }

    @Override
    public List<ProcessingMetadata> getProcessingMetadataByStatus(ProcessingStatus status) {
        log.debug("Fetching processing metadata list for status: {}", status);
        return this.processingMetadataRepository.findByProcessingStatus(status);
    }

    @Override
    public List<ProcessingMetadata> getAllProcessingMetadata() {
        return this.processingMetadataRepository.findAll();
    }

    @Override
    public ProcessingMetadata updateProcessingStatus(String identifier, ProcessingStatus status) {
        log.info("Updating processing status for identifier: {} to {}", identifier, status);

        ProcessingMetadata metadata = getProcessingMetadataByFileId(identifier)
                .or(() -> getProcessingMetadataByDocumentId(identifier))
                .orElseThrow(() -> new NotFoundException("Processing metadata not found for identifier: " + identifier));

        metadata.setProcessingStatus(status);
        if (status == ProcessingStatus.PROCESSED || status == ProcessingStatus.PROCESSING_FAILED) {
            metadata.setEndDateTime(Instant.now());
        }

        return this.processingMetadataRepository.save(metadata);
    }
}
