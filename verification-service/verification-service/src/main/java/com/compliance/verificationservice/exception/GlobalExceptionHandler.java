package com.compliance.verificationservice.exception;

import com.compliance.verificationservice.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            VerificationNotFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleVerificationNotFound(
            VerificationNotFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(
            VerificationAlreadyExistsException.class
    )
    public ResponseEntity<ErrorResponse>
    handleVerificationAlreadyExists(
            VerificationAlreadyExistsException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(
            InvalidVerificationRequestException.class
    )
    public ResponseEntity<ErrorResponse>
    handleInvalidVerificationRequest(
            InvalidVerificationRequestException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(
            ExternalServiceException.class
    )
    public ResponseEntity<ErrorResponse>
    handleExternalServiceException(
            ExternalServiceException exception,
            HttpServletRequest request) {

        LOGGER.error(
                "External service call failed. "
                        + "service={}, statusCode={}, message={}",
                exception.getServiceName(),
                exception.getStatusCode(),
                exception.getMessage(),
                exception
        );

        String message =
                "External service "
                        + exception.getServiceName()
                        + " is unavailable or returned an error";

        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {

            message =
                    exception.getMessage();
        }

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                message,
                request,
                null
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

            validationErrors.putIfAbsent(
                    fieldError.getField(),
                    resolveValidationMessage(
                            fieldError
                    )
            );
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request,
                validationErrors
        );
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

        for (ConstraintViolation<?> violation
                : exception.getConstraintViolations()) {

            String propertyPath =
                    violation.getPropertyPath() == null
                            ? "request"
                            : violation.getPropertyPath()
                            .toString();

            validationErrors.putIfAbsent(
                    propertyPath,
                    safeMessage(
                            violation.getMessage(),
                            "Invalid value"
                    )
            );
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String message =
                buildTypeMismatchMessage(
                        exception
                );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                null
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ErrorResponse>
    handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON",
                request,
                null
        );
    }

    @ExceptionHandler(
            MissingRequestHeaderException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMissingRequestHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request) {

        String message =
                "Required request header is missing: "
                        + exception.getHeaderName();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                null
        );
    }

    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMissingRequestParameter(
            MissingServletRequestParameterException
                    exception,
            HttpServletRequest request) {

        String message =
                "Required request parameter is missing: "
                        + exception.getParameterName();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                null
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
                "Database integrity violation while processing {}",
                getRequestPath(request),
                exception
        );

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing database data",
                request,
                null
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                safeMessage(
                        exception.getMessage(),
                        "Invalid request"
                ),
                request,
                null
        );
    }

    @ExceptionHandler(
            NoHandlerFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleNoHandlerFound(
            NoHandlerFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested API endpoint was not found",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        LOGGER.error(
                "Unexpected application error while processing {}",
                getRequestPath(request),
                exception
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the request",
                request,
                null
        );
    }

    private ResponseEntity<ErrorResponse>
    buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        ErrorResponse errorResponse =
                new ErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        safeMessage(
                                message,
                                status.getReasonPhrase()
                        ),
                        getRequestPath(
                                request
                        )
                );

        if (validationErrors != null
                && !validationErrors.isEmpty()) {

            errorResponse.setValidationErrors(
                    validationErrors
            );
        }

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private String buildTypeMismatchMessage(
            MethodArgumentTypeMismatchException
                    exception) {

        String parameterName =
                exception.getName();

        Class<?> requiredType =
                exception.getRequiredType();

        String providedValue =
                exception.getValue() == null
                        ? "null"
                        : exception.getValue()
                        .toString();

        if (requiredType != null
                && requiredType.isEnum()) {

            Object[] allowedValues =
                    requiredType.getEnumConstants();

            StringBuilder message =
                    new StringBuilder();

            message.append(
                    "Invalid value '"
            );

            message.append(
                    providedValue
            );

            message.append(
                    "' for parameter '"
            );

            message.append(
                    parameterName
            );

            message.append(
                    "'. Allowed values are: "
            );

            if (allowedValues != null) {

                for (int index = 0;
                     index < allowedValues.length;
                     index++) {

                    if (index > 0) {
                        message.append(", ");
                    }

                    message.append(
                            allowedValues[index]
                    );
                }
            }

            return message.toString();
        }

        return "Invalid value '"
                + providedValue
                + "' for parameter '"
                + parameterName
                + "'";
    }

    private String resolveValidationMessage(
            FieldError fieldError) {

        if (fieldError == null) {
            return "Invalid value";
        }

        return safeMessage(
                fieldError.getDefaultMessage(),
                "Invalid value"
        );
    }

    private String safeMessage(
            String message,
            String defaultMessage) {

        if (message == null
                || message.isBlank()) {

            return defaultMessage;
        }

        String normalizedMessage =
                message.trim();

        if (normalizedMessage.length() > 2000) {
            return normalizedMessage.substring(
                    0,
                    2000
            );
        }

        return normalizedMessage;
    }

    private String getRequestPath(
            HttpServletRequest request) {

        if (request == null
                || request.getRequestURI() == null
                || request.getRequestURI().isBlank()) {

            return "unknown";
        }

        return request.getRequestURI();
    }
}