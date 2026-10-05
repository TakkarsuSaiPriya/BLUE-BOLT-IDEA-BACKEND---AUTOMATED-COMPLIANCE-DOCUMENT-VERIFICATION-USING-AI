package com.compliance.ocrservice.exception;

public class DocumentDownloadException
        extends RuntimeException {

    public DocumentDownloadException(
            String message) {

        super(message);
    }

    public DocumentDownloadException(
            String message,
            Throwable cause) {

        super(message, cause);
    }

    public DocumentDownloadException(
            Long documentId,
            Throwable cause) {

        super(
                "Unable to download document with ID: "
                        + documentId,
                cause
        );
    }
}