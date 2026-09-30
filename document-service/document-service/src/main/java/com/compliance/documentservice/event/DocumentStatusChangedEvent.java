package com.compliance.documentservice.event;

import com.compliance.documentservice.enums.DocumentStatus;

import java.time.LocalDateTime;

public class DocumentStatusChangedEvent {

    private String eventId;
    private String eventType;
    private Long documentId;
    private DocumentStatus previousStatus;
    private DocumentStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime occurredAt;

    public DocumentStatusChangedEvent() {
    }

    public DocumentStatusChangedEvent(
            String eventId,
            String eventType,
            Long documentId,
            DocumentStatus previousStatus,
            DocumentStatus newStatus,
            String reason,
            String changedBy,
            LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.documentId = documentId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public DocumentStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(
            DocumentStatus previousStatus) {

        this.previousStatus = previousStatus;
    }

    public DocumentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(
            DocumentStatus newStatus) {

        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(
            String changedBy) {

        this.changedBy = changedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}