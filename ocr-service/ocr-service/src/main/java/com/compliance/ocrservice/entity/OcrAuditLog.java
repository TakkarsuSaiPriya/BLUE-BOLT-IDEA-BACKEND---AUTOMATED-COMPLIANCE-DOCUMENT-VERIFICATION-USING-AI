package com.compliance.ocrservice.entity;

import com.compliance.ocrservice.enums.OcrAuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ocr_audit_logs",
        indexes = {
                @Index(
                        name = "idx_ocr_audit_document",
                        columnList = "document_id"
                ),
                @Index(
                        name = "idx_ocr_audit_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_ocr_audit_username",
                        columnList = "username"
                )
        }
)
public class OcrAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 60
    )
    private OcrAuditAction action;

    @Column(
            name = "document_id"
    )
    private Long documentId;

    @Column(
            name = "ocr_result_id"
    )
    private Long ocrResultId;

    @Column(
            name = "username",
            nullable = false,
            length = 100
    )
    private String username;

    @Column(
            name = "execution_status",
            nullable = false,
            length = 30
    )
    private String executionStatus;

    @Lob
    @Column(
            name = "request_payload",
            columnDefinition = "LONGTEXT"
    )
    private String requestPayload;

    @Lob
    @Column(
            name = "response_payload",
            columnDefinition = "LONGTEXT"
    )
    private String responsePayload;

    @Column(
            name = "error_message",
            length = 2000
    )
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    public OcrAuditLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OcrAuditAction getAction() {
        return action;
    }

    public void setAction(
            OcrAuditAction action) {

        this.action = action;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(
            String executionStatus) {

        this.executionStatus = executionStatus;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(
            String requestPayload) {

        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(
            String responsePayload) {

        this.responsePayload = responsePayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}