package com.compliance.documentservice.dto.response;

import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;

import java.time.LocalDateTime;

public class DocumentUploadResponse {

    private Long documentId;
    private String originalFileName;
    private DocumentType documentType;
    private DocumentStatus status;
    private String uploadedBy;
    private String message;
    private LocalDateTime uploadedAt;

    public DocumentUploadResponse() {
    }

    public DocumentUploadResponse(
            Long documentId,
            String originalFileName,
            DocumentType documentType,
            DocumentStatus status,
            String uploadedBy,
            String message,
            LocalDateTime uploadedAt) {

        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.documentType = documentType;
        this.status = status;
        this.uploadedBy = uploadedBy;
        this.message = message;
        this.uploadedAt = uploadedAt;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(
            LocalDateTime uploadedAt) {

        this.uploadedAt = uploadedAt;
    }
}