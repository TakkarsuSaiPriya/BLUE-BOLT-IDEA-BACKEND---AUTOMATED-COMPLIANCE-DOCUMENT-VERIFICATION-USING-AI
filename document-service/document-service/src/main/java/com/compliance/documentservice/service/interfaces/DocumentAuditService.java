package com.compliance.documentservice.service.interfaces;

import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.enums.AuditActionType;

import java.util.List;

public interface DocumentAuditService {

    DocumentAuditLog saveAuditLog(
            DocumentAuditLog auditLog
    );

    List<DocumentAuditLog> getAuditLogsByDocumentId(
            Long documentId
    );

    List<DocumentAuditLog> getAuditLogsByUsername(
            String username
    );

    List<DocumentAuditLog> getAuditLogsByAction(
            AuditActionType action
    );
}