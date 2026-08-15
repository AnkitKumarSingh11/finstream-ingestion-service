package dev.byankit.finstream_data_ingestion.services;

import dev.byankit.finstream_data_ingestion.entities.FileMetadata;
import dev.byankit.finstream_data_ingestion.enums.FileUploadType;
import dev.byankit.finstream_data_ingestion.exceptions.BadRequestException;
import dev.byankit.finstream_data_ingestion.records.request.FileUploadRequest;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class HDFCUploadServiceImpl implements FileUploadService {
    private final FileMetadataService metadataService;
    private final FileSystem hdfsFileSystem;

    @Value("${hdfs.upload.path}")
    private String hdfsUploadPath;

    @Autowired
    public HDFCUploadServiceImpl(FileMetadataService metadataService, FileSystem hdfsFileSystem) {
        this.metadataService = metadataService;
        this.hdfsFileSystem = hdfsFileSystem;
    }

    @Override
    public FileMetadata uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) throws FileUploadException {
        validateFileType(file);
        validateFileUploadRequest(fileUploadRequest);

        if (file.getContentType() == null) {
            throw new BadRequestException("File content type is null");
        }

        String type = file.getContentType().split("/")[1];
        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + "." + type;

        Path path = new Path(
        hdfsUploadPath + Path.SEPARATOR +
                fileUploadRequest.fileSource() + Path.SEPARATOR +
                fileUploadRequest.fileSourceType() +  Path.SEPARATOR +
                fileName
        );

        try (
            InputStream inputStream = file.getInputStream();
            FSDataOutputStream outputStream = hdfsFileSystem.create(path);
        ) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writeMetadataToDb(
            fileUploadRequest,
            fileId,
            fileName,
            hdfsUploadPath,
            file.getContentType(),
            file.getSize()
        );
    }

    private FileMetadata writeMetadataToDb(
        FileUploadRequest request,
        String fileId,
        String fileName,
        String hdfsUploadPath,
        String contentType,
        long fileSize
    ) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileId(fileId);
        metadata.setContentType(contentType);
        metadata.setFileSize(fileSize);
        metadata.setFilePath(hdfsUploadPath);
        metadata.setSource(request.fileSource());
        metadata.setSourceType(request.fileSourceType());

        return this.metadataService
                .saveFileMetadata(metadata);
    }

    private void validateFileType(MultipartFile file) throws FileUploadException {
        String fileType = file.getContentType();

        if (fileType == null) {
            throw new FileUploadException("File type is null");
        }

        FileUploadType uploadType = switch (fileType) {
            case "application/pdf" -> FileUploadType.PDF;
            case "application/csv" -> FileUploadType.CSV;
            case "application/json" -> FileUploadType.JSON;
            default -> null;
        };

        if (uploadType == null) {
            throw new BadRequestException("File type " + fileType + " is not accepted");
        }
    }

    private FileMetadata saveFileMetadata(FileMetadata fileMetadata) {
        return this.metadataService
                .saveFileMetadata(fileMetadata);
    }

    private void validateFileUploadRequest(FileUploadRequest fileUploadRequest) {
        if (fileUploadRequest == null) {
            throw new BadRequestException("File upload request is null");
        }
    }
}
