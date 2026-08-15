package dev.byankit.finstream_data_ingestion.exceptions;

import dev.byankit.finstream_data_ingestion.records.response.CustomErrorResponse;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomErrorResponse> handleBadRequestException(BadRequestException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.BAD_REQUEST.toString(),
            ex.getMessage(),
            400,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<CustomErrorResponse> handleFileUploadException(FileUploadException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.BAD_REQUEST.toString(),
            ex.getMessage(),
            400,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
