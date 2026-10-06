package com.carpenter.business.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiFieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "The request contains invalid values.", request, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraint(ConstraintViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> malformed(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body is malformed.", request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiError> duplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({UnauthorisedOperationException.class, BadCredentialsException.class})
    ResponseEntity<ApiError> unauthorized(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> uploadTooLarge(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE", "The uploaded file is too large.", request, List.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", request, List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               HttpServletRequest request, List<ApiFieldError> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(),
                status.getReasonPhrase(), code, message, request.getRequestURI(), fields));
    }
}
