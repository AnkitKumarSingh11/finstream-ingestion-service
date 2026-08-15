package dev.byankit.finstream_data_ingestion.enums;

public enum FileSourceType {
    upiservice("For files containing UPI transactions"),
    wallet("File containing wallet payments transactions"),
    bankstatement("File containing bank transactions");

    private final String description;

    FileSourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}