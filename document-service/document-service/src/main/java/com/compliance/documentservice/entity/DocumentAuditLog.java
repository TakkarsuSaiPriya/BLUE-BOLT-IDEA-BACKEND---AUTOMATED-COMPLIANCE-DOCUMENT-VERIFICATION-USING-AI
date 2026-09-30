package com.compliance.documentservice.entity;

import com.compliance.documentservice.enums.AuditActionType;
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
        name = "document_audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_document_id",
                        columnList = "document_id"
                ),
                @Index(
                        name = "idx_audit_username",
                        columnList = "username"
                ),
                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                )
        }
)
public class DocumentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 60
    )
    private AuditActionType action;

    @Column(
            name = "username",
            nullable = false,
            length = 100
    )
    private String username;

    @Column(name = "document_id")
    private Long documentId;

    @Column(
            name = "request_method",
            length = 20
    )
    private String requestMethod;

    @Column(
            name = "request_path",
            length = 500
    )
    private String requestPath;

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
            name = "execution_status",
            nullable = false,
            length = 30
    )
    private String executionStatus;

    @Column(
            name = "ip_address",
            length = 100
    )
    private String ipAddress;

    @Column(
            name = "error_message",
            length = 1000
    )
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    public DocumentAuditLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditActionType getAction() {
        return action;
    }

    public void setAction(
            AuditActionType action) {

        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(
            String requestMethod) {

        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(
            String requestPath) {

        this.requestPath = requestPath;
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

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(
            String executionStatus) {

        this.executionStatus = executionStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(
            String ipAddress) {

        this.ipAddress = ipAddress;
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