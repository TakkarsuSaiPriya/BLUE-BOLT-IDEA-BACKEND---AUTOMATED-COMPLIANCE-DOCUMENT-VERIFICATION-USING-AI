package com.compliance.ocrservice.dto.request;

import jakarta.validation.constraints.Size;

public class OcrRetryRequest {

    @Size(
            max = 30,
            message = "OCR language cannot exceed 30 characters"
    )
    private String language;

    @Size(
            max = 500,
            message = "Retry reason cannot exceed 500 characters"
    )
    private String reason;

    public OcrRetryRequest() {
    }

    public OcrRetryRequest(
            String language,
            String reason) {

        this.language = language;
        this.reason = reason;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(
            String language) {

        this.language = language;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason = reason;
    }
}