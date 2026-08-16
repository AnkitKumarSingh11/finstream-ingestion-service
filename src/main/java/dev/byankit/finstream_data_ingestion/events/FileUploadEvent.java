package dev.byankit.finstream_data_ingestion.events;

import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;

public record FileUploadEvent(
    String fileId,
    String documentId,
    FileSource fileSource,
    FileSourceType fileSourceType,
    String filePath
) {
}
