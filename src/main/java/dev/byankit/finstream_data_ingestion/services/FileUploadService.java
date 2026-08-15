package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    FileMetadata uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) throws FileUploadException;
}
