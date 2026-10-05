package com.compliance.ocrservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentUploadedEvent {

    private String eventId;
    private String eventType;
    private Long documentId;
    private String originalFileName;
    private String contentType;
    private String documentType;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private LocalDateTime occurredAt;

    public DocumentUploadedEvent() {
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType) {

        this.contentType = contentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(
            String documentType) {

        this.documentType = documentType;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(
            String uploadedBy) {

        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(
            LocalDateTime uploadedAt) {

        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}