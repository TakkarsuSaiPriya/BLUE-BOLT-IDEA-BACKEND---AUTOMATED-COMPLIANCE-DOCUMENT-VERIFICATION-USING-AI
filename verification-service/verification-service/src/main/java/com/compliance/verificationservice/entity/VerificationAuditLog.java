package com.compliance.verificationservice.entity;

import com.compliance.verificationservice.enums.VerificationAuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.Locale;

@Entity
@Table(
        name = "verification_audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_verification_result_id",
                        columnList = "verification_result_id"
                ),
                @Index(
                        name = "idx_audit_document_id",
                        columnList = "document_id"
                ),
                @Index(
                        name = "idx_audit_ocr_result_id",
                        columnList = "ocr_result_id"
                ),
                @Index(
                        name = "idx_audit_username",
                        columnList = "username"
                ),
                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_execution_status",
                        columnList = "execution_status"
                ),
                @Index(
                        name = "idx_audit_correlation_id",
                        columnList = "correlation_id"
                ),
                @Index(
                        name = "idx_audit_created_at",
                        columnList = "created_at"
                )
        }
)
public class VerificationAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "verification_result_id"
    )
    private Long verificationResultId;

    @Column(
            name = "document_id"
    )
    private Long documentId;

    @Column(
            name = "ocr_result_id"
    )
    private Long ocrResultId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 100
    )
    private VerificationAuditAction action;

    @Column(
            name = "execution_status",
            nullable = false,
            length = 30
    )
    private String executionStatus;

    @Column(
            name = "username",
            nullable = false,
            length = 150
    )
    private String username;

    @Column(
            name = "details",
            length = 2000
    )
    private String details;

    @Column(
            name = "correlation_id",
            nullable = false,
            length = 100
    )
    private String correlationId;

    @Column(
            name = "client_ip",
            length = 64
    )
    private String clientIp;

    @Column(
            name = "request_path",
            length = 500
    )
    private String requestPath;

    @Column(
            name = "processing_duration_ms"
    )
    private Long processingDurationMs;

    public VerificationAuditLog() {
    }

    @PrePersist
    protected void initializeAuditLog() {

        if (action == null) {
            throw new IllegalStateException(
                    "Verification audit action is required"
            );
        }

        executionStatus =
                normalizeExecutionStatus(
                        executionStatus
                );

        username =
                normalizeUsername(
                        username
                );

        correlationId =
                normalizeCorrelationId(
                        correlationId
                );

        details =
                normalizeNullableText(
                        details,
                        2000
                );

        clientIp =
                normalizeNullableText(
                        clientIp,
                        64
                );

        requestPath =
                normalizeNullableText(
                        requestPath,
                        500
                );

        if (processingDurationMs != null) {
            processingDurationMs =
                    Math.max(
                            processingDurationMs,
                            0L
                    );
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public Long getVerificationResultId() {
        return verificationResultId;
    }

    public void setVerificationResultId(
            Long verificationResultId) {

        this.verificationResultId =
                normalizePositiveId(
                        verificationResultId
                );
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId =
                normalizePositiveId(
                        documentId
                );
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId =
                normalizePositiveId(
                        ocrResultId
                );
    }

    public VerificationAuditAction getAction() {
        return action;
    }

    public void setAction(
            VerificationAuditAction action) {

        this.action = action;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(
            String executionStatus) {

        this.executionStatus =
                normalizeExecutionStatus(
                        executionStatus
                );
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username =
                normalizeUsername(
                        username
                );
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(
            String details) {

        this.details =
                normalizeNullableText(
                        details,
                        2000
                );
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(
            String correlationId) {

        this.correlationId =
                normalizeCorrelationId(
                        correlationId
                );
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(
            String clientIp) {

        this.clientIp =
                normalizeNullableText(
                        clientIp,
                        64
                );
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(
            String requestPath) {

        this.requestPath =
                normalizeNullableText(
                        requestPath,
                        500
                );
    }

    public Long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(
            Long processingDurationMs) {

        if (processingDurationMs == null) {
            this.processingDurationMs = null;
            return;
        }

        this.processingDurationMs =
                Math.max(
                        processingDurationMs,
                        0L
                );
    }

    private Long normalizePositiveId(
            Long value) {

        if (value == null
                || value <= 0L) {

            return null;
        }

        return value;
    }

    private String normalizeExecutionStatus(
            String status) {

        if (status == null
                || status.isBlank()) {

            return "UNKNOWN";
        }

        String normalized =
                status.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return limitText(
                normalized,
                30
        );
    }

    private String normalizeUsername(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "system";
        }

        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return limitText(
                normalized,
                150
        );
    }

    private String normalizeCorrelationId(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "unknown";
        }

        return limitText(
                value.trim(),
                100
        );
    }

    private String normalizeNullableText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return limitText(
                value.trim(),
                maximumLength
        );
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value.length() > maximumLength) {
            return value.substring(
                    0,
                    maximumLength
            );
        }

        return value;
    }
}