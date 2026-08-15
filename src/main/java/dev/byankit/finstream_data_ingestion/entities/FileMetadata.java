package dev.byankit.finstream_data_ingestion.entities;

import dev.byankit.finstream_data_ingestion.enums.FileSource;
import dev.byankit.finstream_data_ingestion.enums.FileSourceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(
    collection = "files_metadata"
)
public class FileMetadata {

    @Id
    private String documentId;

    @Indexed(unique = true)
    private String fileId;

    private String fileName;
    private String contentType;
    private long fileSize;
    private FileSourceType sourceType;
    private FileSource source;
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(FileSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public FileSource getSource() {
        return source;
    }

    public void setSource(FileSource source) {
        this.source = source;
    }
}
