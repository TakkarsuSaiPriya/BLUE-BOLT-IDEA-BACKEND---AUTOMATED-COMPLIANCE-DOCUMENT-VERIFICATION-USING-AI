package com.compliance.documentservice.dto.response;

import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private String fileExtension;
    private Long fileSize;
    private String checksum;
    private DocumentType documentType;
    private DocumentStatus status;
    private String uploadedBy;
    private String description;
    private String statusReason;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(
            String fileExtension) {

        this.fileExtension = fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(
            Long fileSize) {

        this.fileSize = fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(
            String checksum) {

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

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(
            String statusReason) {

        this.statusReason = statusReason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active) {

        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}