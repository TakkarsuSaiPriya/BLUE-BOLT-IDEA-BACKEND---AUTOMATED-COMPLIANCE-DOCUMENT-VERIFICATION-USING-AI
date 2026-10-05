package com.compliance.verificationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ManualVerificationRequest {

    @NotNull(
            message = "OCR result ID is required"
    )
    @Positive(
            message = "OCR result ID must be positive"
    )
    private Long ocrResultId;

    @Size(
            max = 100,
            message = "Document type cannot exceed 100 characters"
    )
    private String documentType;

    private boolean forceReprocess;

    public ManualVerificationRequest() {
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId = ocrResultId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(
            String documentType) {

        if (documentType == null
                || documentType.isBlank()) {

            this.documentType = null;
            return;
        }

        this.documentType =
                documentType.trim();
    }

    public boolean isForceReprocess() {
        return forceReprocess;
    }

    public void setForceReprocess(
            boolean forceReprocess) {

        this.forceReprocess =
                forceReprocess;
    }
}