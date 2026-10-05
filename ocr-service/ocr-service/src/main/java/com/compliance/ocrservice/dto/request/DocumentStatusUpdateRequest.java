package com.compliance.ocrservice.dto.request;

public class DocumentStatusUpdateRequest {

    private String status;
    private String reason;
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

        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason = reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(
            String changedBy) {

        this.changedBy = changedBy;
    }
}