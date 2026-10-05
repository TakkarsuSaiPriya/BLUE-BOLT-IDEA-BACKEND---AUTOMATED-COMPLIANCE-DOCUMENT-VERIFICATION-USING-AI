package com.compliance.verificationservice.exception;

public class InvalidVerificationRequestException
        extends RuntimeException {

    public InvalidVerificationRequestException(
            String message) {

        super(message);
    }

    public InvalidVerificationRequestException(
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );
    }
}