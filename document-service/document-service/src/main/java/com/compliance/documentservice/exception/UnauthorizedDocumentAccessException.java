package com.compliance.documentservice.exception;

public class UnauthorizedDocumentAccessException
        extends RuntimeException {

    public UnauthorizedDocumentAccessException(
            String message) {

        super(message);
    }

    public UnauthorizedDocumentAccessException(
            Long documentId,
            String username) {

        super(
                "User "
                        + username
                        + " is not authorized to access document ID: "
                        + documentId
        );
    }
}