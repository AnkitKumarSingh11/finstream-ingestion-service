package dev.byankit.finstream_data_ingestion.enums;

public enum FileUploadType {
    CSV ("application/csv"),
    JSON ("application/json"),
    PDF ("application/pdf");

    private final String type;

    FileUploadType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
