package com.compliance.documentservice.repository;

import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.enums.AuditActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAuditLogRepository
        extends JpaRepository<DocumentAuditLog, Long> {

    List<DocumentAuditLog> findAllByDocumentIdOrderByCreatedAtDesc(
            Long documentId
    );

    List<DocumentAuditLog> findAllByUsernameOrderByCreatedAtDesc(
            String username
    );

    List<DocumentAuditLog> findAllByActionOrderByCreatedAtDesc(
            AuditActionType action
    );
}