package com.compliance.documentservice.exception;

public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String message) {
        super(message);
    }

    public DuplicateDocumentException(
            String fileName,
            Long existingDocumentId) {

        super(
                "A document with the same content already exists. "
                        + "File: "
                        + fileName
                        + ", existing document ID: "
                        + existingDocumentId
        );
    }
}