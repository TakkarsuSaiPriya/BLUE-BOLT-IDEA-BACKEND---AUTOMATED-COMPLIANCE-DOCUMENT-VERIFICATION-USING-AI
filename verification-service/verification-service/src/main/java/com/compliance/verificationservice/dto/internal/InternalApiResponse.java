package com.compliance.verificationservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    private LocalDateTime timestamp;

    public InternalApiResponse() {
    }

    public InternalApiResponse(
            boolean success,
            String message,
            T data) {

        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
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

    public T getData() {
        return data;
    }

    public void setData(
            T data) {

        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(
            LocalDateTime timestamp) {

        this.timestamp = timestamp;
    }
}