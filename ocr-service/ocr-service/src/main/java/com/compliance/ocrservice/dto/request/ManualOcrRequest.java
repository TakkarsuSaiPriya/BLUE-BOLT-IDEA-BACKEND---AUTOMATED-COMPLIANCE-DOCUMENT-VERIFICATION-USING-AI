package com.compliance.ocrservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ManualOcrRequest {

    @NotNull(
            message = "Document ID is required"
    )
    @Positive(
            message = "Document ID must be a positive number"
    )
    private Long documentId;

    @Size(
            max = 30,
            message = "OCR language cannot exceed 30 characters"
    )
    private String language;

    private boolean forceReprocess;

    public ManualOcrRequest() {
    }

    public ManualOcrRequest(
            Long documentId,
            String language,
            boolean forceReprocess) {

        this.documentId = documentId;
        this.language = language;
        this.forceReprocess = forceReprocess;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(
            String language) {

        this.language = language;
    }

    public boolean isForceReprocess() {
        return forceReprocess;
    }

    public void setForceReprocess(
            boolean forceReprocess) {

        this.forceReprocess = forceReprocess;
    }
}