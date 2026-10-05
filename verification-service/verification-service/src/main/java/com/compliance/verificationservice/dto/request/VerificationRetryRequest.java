package com.compliance.verificationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerificationRetryRequest {

    @NotBlank(
            message = "Retry reason is required"
    )
    @Size(
            max = 1000,
            message = "Retry reason cannot exceed 1000 characters"
    )
    private String reason;

    private boolean refreshOcrResult;

    public VerificationRetryRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason =
                reason == null
                        ? null
                        : reason.trim();
    }

    public boolean isRefreshOcrResult() {
        return refreshOcrResult;
    }

    public void setRefreshOcrResult(
            boolean refreshOcrResult) {

        this.refreshOcrResult =
                refreshOcrResult;
    }
}