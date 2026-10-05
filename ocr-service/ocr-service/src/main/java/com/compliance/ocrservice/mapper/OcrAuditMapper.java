package com.compliance.ocrservice.mapper;

import com.compliance.ocrservice.entity.OcrAuditLog;
import com.compliance.ocrservice.enums.OcrAuditAction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class OcrAuditMapper {

    public OcrAuditLog createAuditLog(
            OcrAuditAction action,
            Long documentId,
            Long ocrResultId,
            String username,
            String executionStatus,
            String requestPayload,
            String responsePayload,
            String errorMessage) {

        OcrAuditLog auditLog =
                new OcrAuditLog();

        auditLog.setAction(action);
        auditLog.setDocumentId(documentId);
        auditLog.setOcrResultId(ocrResultId);

        auditLog.setUsername(
                normalizeUsername(username)
        );

        auditLog.setExecutionStatus(
                normalizeExecutionStatus(
                        executionStatus
                )
        );

        auditLog.setRequestPayload(
                requestPayload
        );

        auditLog.setResponsePayload(
                responsePayload
        );

        auditLog.setErrorMessage(
                errorMessage
        );

        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        return auditLog;
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "system";
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeExecutionStatus(
            String executionStatus) {

        if (executionStatus == null
                || executionStatus.isBlank()) {

            return "UNKNOWN";
        }

        return executionStatus.trim()
                .toUpperCase(Locale.ROOT);
    }
}