package com.compliance.documentservice.service.impl;

import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.enums.AuditActionType;
import com.compliance.documentservice.repository.DocumentAuditLogRepository;
import com.compliance.documentservice.service.interfaces.DocumentAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentAuditServiceImpl
        implements DocumentAuditService {

    private final DocumentAuditLogRepository auditLogRepository;

    public DocumentAuditServiceImpl(
            DocumentAuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public DocumentAuditLog saveAuditLog(
            DocumentAuditLog auditLog) {

        if (auditLog == null) {
            throw new IllegalArgumentException(
                    "Audit log cannot be null"
            );
        }

        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAuditLog> getAuditLogsByDocumentId(
            Long documentId) {

        return auditLogRepository
                .findAllByDocumentIdOrderByCreatedAtDesc(
                        documentId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAuditLog> getAuditLogsByUsername(
            String username) {

        return auditLogRepository
                .findAllByUsernameOrderByCreatedAtDesc(
                        username
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAuditLog> getAuditLogsByAction(
            AuditActionType action) {

        return auditLogRepository
                .findAllByActionOrderByCreatedAtDesc(
                        action
                );
    }
}