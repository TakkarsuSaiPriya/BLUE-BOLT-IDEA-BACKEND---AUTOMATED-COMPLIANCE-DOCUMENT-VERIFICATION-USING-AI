package com.compliance.verificationservice.dto.event;

import java.time.LocalDateTime;

public class VerificationFailedEvent {

    private String eventId;
    private String eventType;
    private Long verificationResultId;
    private Long documentId;
    private Long ocrResultId;
    private String failureReason;
    private String requestedBy;
    private LocalDateTime occurredAt;

    public VerificationFailedEvent() {
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

    public Long getVerificationResultId() {
        return verificationResultId;
    }

    public void setVerificationResultId(
            Long verificationResultId) {

        this.verificationResultId =
                verificationResultId;
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(
            String failureReason) {

        this.failureReason =
                failureReason;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(
            String requestedBy) {

        this.requestedBy =
                requestedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}