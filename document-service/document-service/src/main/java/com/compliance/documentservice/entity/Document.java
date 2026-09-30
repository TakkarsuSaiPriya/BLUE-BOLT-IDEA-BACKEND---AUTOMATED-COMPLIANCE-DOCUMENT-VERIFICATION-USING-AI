package com.compliance.documentservice.entity;

import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(
                        name = "idx_document_uploaded_by",
                        columnList = "uploaded_by"
                ),
                @Index(
                        name = "idx_document_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_document_type",
                        columnList = "document_type"
                ),
                @Index(
                        name = "idx_document_checksum",
                        columnList = "checksum"
                )
        }
)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "stored_file_name",
            nullable = false,
            unique = true,
            length = 255
    )
    private String storedFileName;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(
            name = "file_extension",
            nullable = false,
            length = 20
    )
    private String fileExtension;

    @Column(
            name = "file_size",
            nullable = false
    )
    private Long fileSize;

    @Column(
            name = "storage_path",
            nullable = false,
            length = 1000
    )
    private String storagePath;

    @Column(
            name = "checksum",
            nullable = false,
            length = 64
    )
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "document_type",
            nullable = false,
            length = 50
    )
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private DocumentStatus status;

    @Column(
            name = "uploaded_by",
            nullable = false,
            length = 100
    )
    private String uploadedBy;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "status_reason",
            length = 500
    )
    private String statusReason;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    public Document() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(
            String storedFileName) {

        this.storedFileName = storedFileName;
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

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(
            String storagePath) {

        this.storagePath = storagePath;
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

    public void setActive(boolean active) {
        this.active = active;
    }
}