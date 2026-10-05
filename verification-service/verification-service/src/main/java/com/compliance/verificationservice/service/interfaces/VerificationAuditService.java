package com.compliance.verificationservice.service.interfaces;

import com.compliance.verificationservice.dto.response.VerificationAuditLogResponse;
import com.compliance.verificationservice.enums.VerificationAuditAction;

import java.util.List;

public interface VerificationAuditService {

    void recordAudit(
            Long verificationResultId,
            Long documentId,
            Long ocrResultId,
            VerificationAuditAction action,
            String executionStatus,
            String username,
            String details,
            String correlationId,
            String clientIp,
            String requestPath,
            Long processingDurationMs
    );

    List<VerificationAuditLogResponse>
    getAllAuditLogs();

    List<VerificationAuditLogResponse>
    getAuditLogsByVerificationResultId(
            Long verificationResultId
    );

    List<VerificationAuditLogResponse>
    getAuditLogsByDocumentId(
            Long documentId
    );

    List<VerificationAuditLogResponse>
    getAuditLogsByOcrResultId(
            Long ocrResultId
    );

    List<VerificationAuditLogResponse>
    getAuditLogsByUsername(
            String username
    );

    List<VerificationAuditLogResponse>
    getAuditLogsByAction(
            VerificationAuditAction action
    );

    List<VerificationAuditLogResponse>
    getAuditLogsByCorrelationId(
            String correlationId
    );
}