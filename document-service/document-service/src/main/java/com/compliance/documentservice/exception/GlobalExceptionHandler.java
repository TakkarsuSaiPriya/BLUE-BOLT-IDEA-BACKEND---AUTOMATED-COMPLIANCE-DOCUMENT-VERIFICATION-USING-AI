package com.compliance.documentservice.exception;

import com.compliance.documentservice.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(
            DocumentNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDocument(
            DuplicateDocumentException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDocument(
            InvalidDocumentException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(
            FileStorageException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthentication(
            JwtAuthenticationException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UnauthorizedDocumentAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedDocumentAccess(
            UnauthorizedDocumentAccessException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.FORBIDDEN,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        return buildResponse(
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            validationErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorResponse errorResponse =
                createErrorResponse(
                        "Request validation failed",
                        HttpStatus.BAD_REQUEST,
                        request.getRequestURI()
                );

        errorResponse.setValidationErrors(
                validationErrors
        );

        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        validationErrors.put(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                );

        ErrorResponse errorResponse =
                createErrorResponse(
                        "Request constraint validation failed",
                        HttpStatus.BAD_REQUEST,
                        request.getRequestURI()
                );

        errorResponse.setValidationErrors(
                validationErrors
        );

        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaximumUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request) {

        return buildResponse(
                "Uploaded file exceeds the maximum allowed size of 10 MB",
                HttpStatus.PAYLOAD_TOO_LARGE,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestPart(
            MissingServletRequestPartException exception,
            HttpServletRequest request) {

        String message =
                "Required multipart field is missing: "
                        + exception.getRequestPartName();

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        String message =
                "Required request parameter is missing: "
                        + exception.getParameterName();

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String requiredType =
                exception.getRequiredType() == null
                        ? "required type"
                        : exception.getRequiredType()
                        .getSimpleName();

        String message =
                "Invalid value for parameter '"
                        + exception.getName()
                        + "'. Expected type: "
                        + requiredType;

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildResponse(
                "Request body is missing, malformed, or contains an invalid value",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        return buildResponse(
                "The request content type is not supported",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return buildResponse(
                "The request conflicts with existing document data",
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        return buildResponse(
                "An unexpected error occurred while processing the request",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            String message,
            HttpStatus status,
            String path) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        message,
                        status,
                        path
                );

        return new ResponseEntity<>(
                errorResponse,
                status
        );
    }

    private ErrorResponse createErrorResponse(
            String message,
            HttpStatus status,
            String path) {

        ErrorResponse errorResponse =
                new ErrorResponse();

        errorResponse.setSuccess(false);
        errorResponse.setMessage(message);
        errorResponse.setStatus(status.value());
        errorResponse.setPath(path);
        errorResponse.setTimestamp(LocalDateTime.now());

        return errorResponse;
    }
}