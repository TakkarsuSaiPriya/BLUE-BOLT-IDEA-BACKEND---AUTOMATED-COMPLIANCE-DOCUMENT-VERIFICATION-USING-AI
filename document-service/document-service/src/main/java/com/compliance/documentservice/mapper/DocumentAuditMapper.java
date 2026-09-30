package com.compliance.documentservice.mapper;

import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.enums.AuditActionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DocumentAuditMapper {

    public DocumentAuditLog createAuditLog(
            AuditActionType action,
            String username,
            Long documentId,
            String requestMethod,
            String requestPath,
            String requestPayload,
            String responsePayload,
            String executionStatus,
            String ipAddress,
            String errorMessage) {

        DocumentAuditLog auditLog =
                new DocumentAuditLog();

        auditLog.setAction(action);
        auditLog.setUsername(normalizeUsername(username));
        auditLog.setDocumentId(documentId);
        auditLog.setRequestMethod(requestMethod);
        auditLog.setRequestPath(requestPath);
        auditLog.setRequestPayload(requestPayload);
        auditLog.setResponsePayload(responsePayload);
        auditLog.setExecutionStatus(executionStatus);
        auditLog.setIpAddress(ipAddress);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setCreatedAt(LocalDateTime.now());

        return auditLog;
    }

    private String normalizeUsername(
            String username) {

        if (username == null || username.isBlank()) {
            return "SYSTEM";
        }

        return username;
    }
}