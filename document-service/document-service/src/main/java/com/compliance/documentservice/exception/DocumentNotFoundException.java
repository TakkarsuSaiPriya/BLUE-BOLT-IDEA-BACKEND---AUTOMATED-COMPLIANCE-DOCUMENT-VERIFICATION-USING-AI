package com.compliance.documentservice.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(Long documentId) {
        super("Document not found with ID: " + documentId);
    }
}