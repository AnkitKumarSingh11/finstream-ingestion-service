package dev.byankit.finstream_data_ingestion.records.response;

import java.util.Date;

public record CustomErrorResponse(
    String code,
    String message,
    int status,
    Date timestamp
) {
}
