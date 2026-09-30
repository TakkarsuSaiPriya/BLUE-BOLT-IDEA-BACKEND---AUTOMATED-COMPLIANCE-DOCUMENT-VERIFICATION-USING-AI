package com.compliance.documentservice.event;

import java.time.LocalDateTime;

public class DocumentDeletedEvent {

    private String eventId;
    private String eventType;
    private Long documentId;
    private String originalFileName;
    private String uploadedBy;
    private String deletedBy;
    private LocalDateTime occurredAt;

    public DocumentDeletedEvent() {
    }

    public DocumentDeletedEvent(
            String eventId,
            String eventType,
            Long documentId,
            String originalFileName,
            String uploadedBy,
            String deletedBy,
            LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.uploadedBy = uploadedBy;
        this.deletedBy = deletedBy;
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName = originalFileName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(
            String uploadedBy) {

        this.uploadedBy = uploadedBy;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(
            String deletedBy) {

        this.deletedBy = deletedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}