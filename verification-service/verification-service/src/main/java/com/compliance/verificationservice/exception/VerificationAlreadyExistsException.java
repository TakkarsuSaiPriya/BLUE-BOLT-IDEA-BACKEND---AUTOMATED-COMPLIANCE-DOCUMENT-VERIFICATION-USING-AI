package com.compliance.verificationservice.exception;

public class VerificationAlreadyExistsException
        extends RuntimeException {

    public VerificationAlreadyExistsException(
            String message) {

        super(message);
    }

    public static VerificationAlreadyExistsException
    forOcrResult(
            Long ocrResultId) {

        return new VerificationAlreadyExistsException(
                "A verification result already exists for OCR result ID: "
                        + ocrResultId
        );
    }
}