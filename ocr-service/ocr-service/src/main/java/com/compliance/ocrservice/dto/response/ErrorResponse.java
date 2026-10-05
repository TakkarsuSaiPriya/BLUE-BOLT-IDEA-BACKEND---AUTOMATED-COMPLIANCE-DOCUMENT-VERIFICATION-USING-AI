package com.compliance.ocrservice.dto.response;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorResponse {

    private boolean success;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;

    private Map<String, String> validationErrors =
            new LinkedHashMap<>();

    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(
            String message,
            int status,
            String path,
            LocalDateTime timestamp) {

        this.success = false;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(
            boolean success) {

        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(
            int status) {

        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(
            String path) {

        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(
            LocalDateTime timestamp) {

        this.timestamp = timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(
            Map<String, String> validationErrors) {

        if (validationErrors == null) {
            this.validationErrors =
                    new LinkedHashMap<>();

            return;
        }

        this.validationErrors =
                validationErrors;
    }
}