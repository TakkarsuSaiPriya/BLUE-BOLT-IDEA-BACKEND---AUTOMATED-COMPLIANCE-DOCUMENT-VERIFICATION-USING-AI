package com.compliance.verificationservice.exception;

public class VerificationNotFoundException
        extends RuntimeException {

    public VerificationNotFoundException(
            String message) {

        super(message);
    }

    public VerificationNotFoundException(
            Long verificationResultId) {

        super(
                "Verification result not found with ID: "
                        + verificationResultId
        );
    }
}