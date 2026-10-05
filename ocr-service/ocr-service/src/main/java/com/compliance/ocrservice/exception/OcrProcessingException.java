package com.compliance.ocrservice.exception;

public class OcrProcessingException
        extends RuntimeException {

    public OcrProcessingException(
            String message) {

        super(message);
    }

    public OcrProcessingException(
            String message,
            Throwable cause) {

        super(message, cause);
    }

    public OcrProcessingException(
            Long documentId,
            String message,
            Throwable cause) {

        super(
                "OCR processing failed for document ID "
                        + documentId
                        + ": "
                        + message,
                cause
        );
    }
}