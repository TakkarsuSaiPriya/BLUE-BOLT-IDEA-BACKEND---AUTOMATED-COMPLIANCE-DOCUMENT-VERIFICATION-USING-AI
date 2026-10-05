package com.compliance.verificationservice.dto.response;

import com.compliance.verificationservice.enums.VerificationAuditAction;

import java.time.LocalDateTime;

public class VerificationAuditLogResponse {

    private Long id;
    private Long verificationResultId;
    private Long documentId;
    private Long ocrResultId;
    private VerificationAuditAction action;
    private String executionStatus;
    private String username;
    private String details;
    private String correlationId;
    private String clientIp;
    private String requestPath;
    private Long processingDurationMs;
    private LocalDateTime createdAt;

    public VerificationAuditLogResponse() {
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
                verificationResultId;
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
                executionStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(
            String details) {

        this.details = details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(
            String correlationId) {

        this.correlationId =
                correlationId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(
            String clientIp) {

        this.clientIp = clientIp;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(
            String requestPath) {

        this.requestPath = requestPath;
    }

    public Long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(
            Long processingDurationMs) {

        this.processingDurationMs =
                processingDurationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}