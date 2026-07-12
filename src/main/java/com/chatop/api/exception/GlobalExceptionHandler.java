package com.chatop.api.exception;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.exception.ApiErrorResponse.FieldValidationError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();

        return ResponseEntity.status(statusCode)
            .body(errorResponse(statusCode, responseStatusMessage(exception, statusCode), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        return validationErrorResponse(exception, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
        BindException exception,
        HttpServletRequest request
    ) {
        return validationErrorResponse(exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error("Unexpected API error on {}", request.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                request.getRequestURI()
            ));
    }

    private ResponseEntity<ApiErrorResponse> validationErrorResponse(
        BindException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                VALIDATION_FAILED_MESSAGE,
                request.getRequestURI(),
                fieldErrors(exception)
            ));
    }

    private ApiErrorResponse errorResponse(HttpStatusCode statusCode, String message, String path) {
        return new ApiErrorResponse(
            statusCode.value(),
            reasonPhrase(statusCode),
            message,
            path,
            List.of()
        );
    }

    private String responseStatusMessage(ResponseStatusException exception, HttpStatusCode statusCode) {
        if (exception.getReason() == null || exception.getReason().isBlank()) {
            return reasonPhrase(statusCode);
        }

        return exception.getReason();
    }

    private List<FieldValidationError> fieldErrors(BindException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(fieldError -> new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
    }

    private String reasonPhrase(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        if (status == null) {
            return "HTTP " + statusCode.value();
        }

        return status.getReasonPhrase();
    }
}
