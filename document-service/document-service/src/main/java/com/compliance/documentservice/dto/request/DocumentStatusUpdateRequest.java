package com.compliance.documentservice.dto.request;

import com.compliance.documentservice.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentStatusUpdateRequest {

    @NotNull(message = "Document status is required")
    private DocumentStatus status;

    @Size(
            max = 500,
            message = "Status reason cannot exceed 500 characters"
    )
    private String reason;

    public DocumentStatusUpdateRequest() {
    }

    public DocumentStatusUpdateRequest(
            DocumentStatus status,
            String reason) {

        this.status = status;
        this.reason = reason;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(
            DocumentStatus status) {

        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason = reason;
    }
}