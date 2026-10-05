package com.compliance.verificationservice.dto.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentStatusUpdateRequest {

    @NotBlank(
            message = "Document status is required"
    )
    @Size(
            max = 100,
            message = "Document status cannot exceed 100 characters"
    )
    private String status;

    @Size(
            max = 1000,
            message = "Status reason cannot exceed 1000 characters"
    )
    private String reason;

    @NotBlank(
            message = "Changed-by value is required"
    )
    @Size(
            max = 150,
            message = "Changed-by value cannot exceed 150 characters"
    )
    private String changedBy;

    public DocumentStatusUpdateRequest() {
    }

    public DocumentStatusUpdateRequest(
            String status,
            String reason,
            String changedBy) {

        this.status = status;
        this.reason = reason;
        this.changedBy = changedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status =
                status == null
                        ? null
                        : status.trim();
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

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(
            String changedBy) {

        this.changedBy =
                changedBy == null
                        ? null
                        : changedBy.trim();
    }
}