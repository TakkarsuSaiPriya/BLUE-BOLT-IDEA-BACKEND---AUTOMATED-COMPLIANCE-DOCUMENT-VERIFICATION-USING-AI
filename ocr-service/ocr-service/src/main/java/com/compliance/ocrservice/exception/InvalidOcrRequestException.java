package com.compliance.ocrservice.exception;

public class InvalidOcrRequestException
        extends RuntimeException {

    public InvalidOcrRequestException(
            String message) {

        super(message);
    }

    public InvalidOcrRequestException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}