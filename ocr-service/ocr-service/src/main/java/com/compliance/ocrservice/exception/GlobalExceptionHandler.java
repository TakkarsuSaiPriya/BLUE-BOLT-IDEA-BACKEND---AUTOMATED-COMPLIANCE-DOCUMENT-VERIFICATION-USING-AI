package com.compliance.ocrservice.exception;

import com.compliance.ocrservice.dto.response.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            OcrResultNotFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleOcrResultNotFound(
            OcrResultNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(
            InvalidOcrRequestException.class
    )
    public ResponseEntity<ErrorResponse>
    handleInvalidOcrRequest(
            InvalidOcrRequestException exception,
            HttpServletRequest request) {

        return buildResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(
            DocumentDownloadException.class
    )
    public ResponseEntity<ErrorResponse>
    handleDocumentDownload(
            DocumentDownloadException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document download failed: {}",
                exception.getMessage(),
                exception
        );

        return buildResponse(
                exception.getMessage(),
                HttpStatus.BAD_GATEWAY,
                request
        );
    }

    @ExceptionHandler(
            OcrProcessingException.class
    )
    public ResponseEntity<ErrorResponse>
    handleOcrProcessing(
            OcrProcessingException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "OCR processing failed: {}",
                exception.getMessage(),
                exception
        );

        return buildResponse(
                exception.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                request
        );
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult()
                .getFieldErrors()) {

            String errorMessage =
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value"
                            : fieldError.getDefaultMessage();

            validationErrors.putIfAbsent(
                    fieldError.getField(),
                    errorMessage
            );
        }

        ErrorResponse errorResponse =
                createErrorResponse(
                        "Request validation failed",
                        HttpStatus.BAD_REQUEST,
                        getRequestPath(request)
                );

        errorResponse.setValidationErrors(
                validationErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        validationErrors.put(
                                violation.getPropertyPath()
                                        .toString(),
                                violation.getMessage()
                        )
                );

        ErrorResponse errorResponse =
                createErrorResponse(
                        "Request constraint validation failed",
                        HttpStatus.BAD_REQUEST,
                        getRequestPath(request)
                );

        errorResponse.setValidationErrors(
                validationErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {

        String message =
                "Required request parameter is missing: "
                        + exception.getParameterName();

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
    handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String expectedType =
                exception.getRequiredType() == null
                        ? "required type"
                        : exception.getRequiredType()
                        .getSimpleName();

        String invalidValue =
                exception.getValue() == null
                        ? "null"
                        : exception.getValue()
                        .toString();

        String message =
                "Invalid value '"
                        + invalidValue
                        + "' for parameter '"
                        + exception.getName()
                        + "'. Expected type: "
                        + expectedType;

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ErrorResponse>
    handleUnreadableRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildResponse(
                "Request body is missing, malformed, "
                        + "or contains an invalid value",
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(
            HttpMediaTypeNotSupportedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        return buildResponse(
                "The request content type is not supported",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request
        );
    }

    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleUnsupportedRequestMethod(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        String message =
                "HTTP method "
                        + exception.getMethod()
                        + " is not supported for this endpoint";

        return buildResponse(
                message,
                HttpStatus.METHOD_NOT_ALLOWED,
                request
        );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "OCR database integrity violation",
                exception
        );

        return buildResponse(
                "The OCR request conflicts with existing data",
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        return buildResponse(
                "You do not have permission "
                        + "to access this resource",
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(
            AuthenticationException.class
    )
    public ResponseEntity<ErrorResponse>
    handleAuthenticationFailure(
            AuthenticationException exception,
            HttpServletRequest request) {

        return buildResponse(
                "Authentication is required "
                        + "to access this resource",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(
            FeignException.NotFound.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignNotFound(
            FeignException.NotFound exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service returned 404 while "
                        + "OCR Service requested document content",
                exception
        );

        return buildResponse(
                "The requested document was not found "
                        + "in Document Service",
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(
            FeignException.Unauthorized.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignUnauthorized(
            FeignException.Unauthorized exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service rejected the internal "
                        + "OCR Service authentication",
                exception
        );

        return buildResponse(
                "OCR Service could not authenticate "
                        + "with Document Service",
                HttpStatus.BAD_GATEWAY,
                request
        );
    }

    @ExceptionHandler(
            FeignException.Forbidden.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignForbidden(
            FeignException.Forbidden exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service denied the internal "
                        + "OCR Service request",
                exception
        );

        return buildResponse(
                "OCR Service is not authorized "
                        + "to retrieve this document",
                HttpStatus.BAD_GATEWAY,
                request
        );
    }

    @ExceptionHandler(
            FeignException.BadRequest.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignBadRequest(
            FeignException.BadRequest exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service rejected an OCR Service request",
                exception
        );

        return buildResponse(
                "Document Service rejected the OCR request",
                HttpStatus.BAD_GATEWAY,
                request
        );
    }

    @ExceptionHandler(
            FeignException.ServiceUnavailable.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignServiceUnavailable(
            FeignException.ServiceUnavailable exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service is currently unavailable",
                exception
        );

        return buildResponse(
                "Document Service is currently unavailable",
                HttpStatus.SERVICE_UNAVAILABLE,
                request
        );
    }

    @ExceptionHandler(
            FeignException.class
    )
    public ResponseEntity<ErrorResponse>
    handleFeignException(
            FeignException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Document Service communication failed "
                        + "with HTTP status {}",
                exception.status(),
                exception
        );

        return buildResponse(
                "Unable to communicate with Document Service",
                HttpStatus.BAD_GATEWAY,
                request
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ErrorResponse>
    handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildResponse(
                safeMessage(
                        exception,
                        "The request contains an invalid value"
                ),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<ErrorResponse>
    handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Invalid OCR processing state",
                exception
        );

        return buildResponse(
                safeMessage(
                        exception,
                        "OCR processing is currently unavailable"
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Unexpected OCR Service error",
                exception
        );

        return buildResponse(
                "An unexpected error occurred "
                        + "while processing the OCR request",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private ResponseEntity<ErrorResponse>
    buildResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        sanitizeResponseMessage(
                                message,
                                status
                        ),
                        status,
                        getRequestPath(request)
                );

        return ResponseEntity
                .status(status)
                .body(errorResponse);
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
        errorResponse.setTimestamp(
                LocalDateTime.now()
        );

        return errorResponse;
    }

    private String getRequestPath(
            HttpServletRequest request) {

        if (request == null) {
            return null;
        }

        return request.getRequestURI();
    }

    private String safeMessage(
            Throwable exception,
            String defaultMessage) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return defaultMessage;
        }

        String message =
                exception.getMessage()
                        .trim();

        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }

        return message;
    }

    private String sanitizeResponseMessage(
            String message,
            HttpStatus status) {

        if (message == null || message.isBlank()) {
            return status.getReasonPhrase();
        }

        String normalizedMessage =
                message.trim();

        if (normalizedMessage.length() > 1000) {
            return normalizedMessage.substring(
                    0,
                    1000
            );
        }

        return normalizedMessage;
    }
}