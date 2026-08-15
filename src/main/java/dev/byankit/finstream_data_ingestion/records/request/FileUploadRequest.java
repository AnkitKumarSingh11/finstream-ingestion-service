package dev.byankit.finstream_data_ingestion.records.request;

import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;

public record FileUploadRequest(
    FileSource fileSource,
    FileSourceType fileSourceType
) {

}
