package com.compliance.ocrservice.repository;

import com.compliance.ocrservice.entity.OcrAuditLog;
import com.compliance.ocrservice.enums.OcrAuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrAuditLogRepository
        extends JpaRepository<OcrAuditLog, Long> {

    List<OcrAuditLog>
    findAllByDocumentIdOrderByCreatedAtDesc(
            Long documentId
    );

    List<OcrAuditLog>
    findAllByOcrResultIdOrderByCreatedAtDesc(
            Long ocrResultId
    );

    List<OcrAuditLog>
    findAllByUsernameOrderByCreatedAtDesc(
            String username
    );

    List<OcrAuditLog>
    findAllByActionOrderByCreatedAtDesc(
            OcrAuditAction action
    );

    List<OcrAuditLog>
    findAllByExecutionStatusOrderByCreatedAtDesc(
            String executionStatus
    );
}