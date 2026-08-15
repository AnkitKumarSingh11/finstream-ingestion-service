package dev.byankit.finstream_data_ingestion.enums;

public enum FileSource {
    paytm("Paytm App"),
    payzap("HDFC PayZapp App"),
    hdfc("HDFC Bank"),
    sbi("SBI Bank");

    private final String name;

    FileSource(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
