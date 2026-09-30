package com.compliance.documentservice.event;

import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;

import java.time.LocalDateTime;

public class DocumentUploadedEvent {

    private String eventId;
    private String eventType;
    private Long documentId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String checksum;
    private DocumentType documentType;
    private DocumentStatus status;
    private String uploadedBy;
    private LocalDateTime occurredAt;

    public DocumentUploadedEvent() {
    }

    public DocumentUploadedEvent(
            String eventId,
            String eventType,
            Long documentId,
            String originalFileName,
            String contentType,
            Long fileSize,
            String checksum,
            DocumentType documentType,
            DocumentStatus status,
            String uploadedBy,
            LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.documentType = documentType;
        this.status = status;
        this.uploadedBy = uploadedBy;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType) {

        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(
            DocumentType documentType) {

        this.documentType = documentType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(
            DocumentStatus status) {

        this.status = status;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(
            String uploadedBy) {

        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}