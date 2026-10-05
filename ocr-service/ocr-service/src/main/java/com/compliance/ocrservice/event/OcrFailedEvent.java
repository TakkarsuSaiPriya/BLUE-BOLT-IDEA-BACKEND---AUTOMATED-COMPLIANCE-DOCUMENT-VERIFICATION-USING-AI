package com.compliance.ocrservice.event;

import java.time.LocalDateTime;

public class OcrFailedEvent {

    private String eventId;
    private String eventType;
    private Long documentId;
    private Long ocrResultId;
    private String originalFileName;
    private String requestedBy;
    private String errorMessage;
    private int deliveryAttempt;
    private boolean retryable;
    private LocalDateTime failedAt;
    private LocalDateTime occurredAt;

    public OcrFailedEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(
            String eventId) {

        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(
            String eventType) {

        this.eventType = eventType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId = ocrResultId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName =
                originalFileName;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(
            String requestedBy) {

        this.requestedBy = requestedBy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public int getDeliveryAttempt() {
        return deliveryAttempt;
    }

    public void setDeliveryAttempt(
            int deliveryAttempt) {

        this.deliveryAttempt = deliveryAttempt;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(
            boolean retryable) {

        this.retryable = retryable;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(
            LocalDateTime failedAt) {

        this.failedAt = failedAt;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}