package com.compliance.ocrservice.service.interfaces;

import com.compliance.ocrservice.entity.OcrAuditLog;
import com.compliance.ocrservice.enums.OcrAuditAction;

import java.util.List;

public interface OcrAuditService {

    OcrAuditLog saveAuditLog(
            OcrAuditLog auditLog
    );

    void recordAudit(
            OcrAuditAction action,
            Long documentId,
            Long ocrResultId,
            String username,
            String executionStatus,
            String requestPayload,
            String responsePayload,
            String errorMessage
    );

    List<OcrAuditLog> getByDocumentId(
            Long documentId
    );

    List<OcrAuditLog> getByOcrResultId(
            Long ocrResultId
    );

    List<OcrAuditLog> getByUsername(
            String username
    );

    List<OcrAuditLog> getByAction(
            OcrAuditAction action
    );

    List<OcrAuditLog> getByExecutionStatus(
            String executionStatus
    );
}