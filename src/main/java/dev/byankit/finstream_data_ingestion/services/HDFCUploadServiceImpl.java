package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.config.HdfsProperties;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @deprecated Use {@link HDFSUploadServiceImpl} instead.
 */
@Deprecated
public class HDFCUploadServiceImpl extends HDFSUploadServiceImpl {

    public HDFCUploadServiceImpl(
            FileMetadataService metadataService,
            FileSystem hdfsFileSystem,
            ProcessingMetadataService processingMetadataService,
            HdfsProperties hdfsProperties,
            ApplicationEventPublisher eventPublisher
    ) {
        super(metadataService, hdfsFileSystem, processingMetadataService, hdfsProperties, eventPublisher);
    }
}
