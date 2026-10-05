package com.compliance.verificationservice.repository;

import com.compliance.verificationservice.entity.VerificationAuditLog;
import com.compliance.verificationservice.enums.VerificationAuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationAuditLogRepository
        extends JpaRepository<VerificationAuditLog, Long> {

    List<VerificationAuditLog>
    findAllByOrderByCreatedAtDesc();

    List<VerificationAuditLog>
    findAllByDocumentIdOrderByCreatedAtDesc(
            Long documentId
    );

    List<VerificationAuditLog>
    findAllByOcrResultIdOrderByCreatedAtDesc(
            Long ocrResultId
    );

    List<VerificationAuditLog>
    findAllByVerificationResultIdOrderByCreatedAtDesc(
            Long verificationResultId
    );

    List<VerificationAuditLog>
    findAllByUsernameOrderByCreatedAtDesc(
            String username
    );

    List<VerificationAuditLog>
    findAllByActionOrderByCreatedAtDesc(
            VerificationAuditAction action
    );

    List<VerificationAuditLog>
    findAllByExecutionStatusOrderByCreatedAtDesc(
            String executionStatus
    );

    List<VerificationAuditLog>
    findAllByCorrelationIdOrderByCreatedAtAsc(
            String correlationId
    );
}