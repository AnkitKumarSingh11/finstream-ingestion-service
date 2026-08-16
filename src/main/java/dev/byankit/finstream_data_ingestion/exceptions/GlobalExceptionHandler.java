package dev.byankit.finstream_data_ingestion.exceptions;

import dev.byankit.finstream_data_ingestion.records.response.CustomErrorResponse;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleNotFoundException(NotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.NOT_FOUND.toString(),
            ex.getMessage(),
            404,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.NOT_FOUND.toString(),
            ex.getMessage(),
            404,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CustomErrorResponse> handleConflictException(ConflictException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.CONFLICT.toString(),
            ex.getMessage(),
            409,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CustomErrorResponse> handleTypeMismatchException(Exception ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Invalid request argument or parameter value: " + ex.getMessage(),
            400,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGenericException(Exception ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "An unexpected internal error occurred: " + ex.getMessage(),
            500,
            new Date(System.currentTimeMillis())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
