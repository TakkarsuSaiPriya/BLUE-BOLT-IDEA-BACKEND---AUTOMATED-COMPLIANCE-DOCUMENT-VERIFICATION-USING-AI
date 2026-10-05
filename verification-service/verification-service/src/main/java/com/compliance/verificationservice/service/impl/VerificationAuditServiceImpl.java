package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.dto.response.VerificationAuditLogResponse;
import com.compliance.verificationservice.entity.VerificationAuditLog;
import com.compliance.verificationservice.enums.VerificationAuditAction;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.repository.VerificationAuditLogRepository;
import com.compliance.verificationservice.service.interfaces.VerificationAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VerificationAuditServiceImpl
        implements VerificationAuditService {

    private final VerificationAuditLogRepository
            verificationAuditLogRepository;

    public VerificationAuditServiceImpl(
            VerificationAuditLogRepository
                    verificationAuditLogRepository) {

        this.verificationAuditLogRepository =
                verificationAuditLogRepository;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void recordAudit(
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
            Long processingDurationMs) {

        if (action == null) {
            throw new InvalidVerificationRequestException(
                    "Audit action is required"
            );
        }

        VerificationAuditLog auditLog =
                new VerificationAuditLog();

        auditLog.setVerificationResultId(
                verificationResultId
        );

        auditLog.setDocumentId(
                documentId
        );

        auditLog.setOcrResultId(
                ocrResultId
        );

        auditLog.setAction(
                action
        );

        auditLog.setExecutionStatus(
                executionStatus
        );

        auditLog.setUsername(
                username
        );

        auditLog.setDetails(
                details
        );

        auditLog.setCorrelationId(
                correlationId
        );

        auditLog.setClientIp(
                clientIp
        );

        auditLog.setRequestPath(
                requestPath
        );

        auditLog.setProcessingDurationMs(
                processingDurationMs
        );

        verificationAuditLogRepository.save(
                auditLog
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAllAuditLogs() {

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByOrderByCreatedAtDesc()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByVerificationResultId(
            Long verificationResultId) {

        validatePositiveId(
                verificationResultId,
                "Verification result ID"
        );

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByVerificationResultIdOrderByCreatedAtDesc(
                                verificationResultId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByDocumentId(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByDocumentIdOrderByCreatedAtDesc(
                                documentId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByOcrResultId(
            Long ocrResultId) {

        validatePositiveId(
                ocrResultId,
                "OCR result ID"
        );

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByOcrResultIdOrderByCreatedAtDesc(
                                ocrResultId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Username is required"
            );
        }

        String normalizedUsername =
                username.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByUsernameOrderByCreatedAtDesc(
                                normalizedUsername
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByAction(
            VerificationAuditAction action) {

        if (action == null) {
            throw new InvalidVerificationRequestException(
                    "Audit action is required"
            );
        }

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByActionOrderByCreatedAtDesc(
                                action
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationAuditLogResponse>
    getAuditLogsByCorrelationId(
            String correlationId) {

        if (correlationId == null
                || correlationId.isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Correlation ID is required"
            );
        }

        return toResponseList(
                verificationAuditLogRepository
                        .findAllByCorrelationIdOrderByCreatedAtAsc(
                                correlationId.trim()
                        )
        );
    }

    private VerificationAuditLogResponse toResponse(
            VerificationAuditLog entity) {

        if (entity == null) {
            return null;
        }

        VerificationAuditLogResponse response =
                new VerificationAuditLogResponse();

        response.setId(
                entity.getId()
        );

        response.setVerificationResultId(
                entity.getVerificationResultId()
        );

        response.setDocumentId(
                entity.getDocumentId()
        );

        response.setOcrResultId(
                entity.getOcrResultId()
        );

        response.setAction(
                entity.getAction()
        );

        response.setExecutionStatus(
                entity.getExecutionStatus()
        );

        response.setUsername(
                entity.getUsername()
        );

        response.setDetails(
                entity.getDetails()
        );

        response.setCorrelationId(
                entity.getCorrelationId()
        );

        response.setClientIp(
                entity.getClientIp()
        );

        response.setRequestPath(
                entity.getRequestPath()
        );

        response.setProcessingDurationMs(
                entity.getProcessingDurationMs()
        );

        response.setCreatedAt(
                entity.getCreatedAt()
        );

        return response;
    }

    private List<VerificationAuditLogResponse>
    toResponseList(
            List<VerificationAuditLog> entities) {

        List<VerificationAuditLogResponse> responses =
                new ArrayList<>();

        if (entities == null
                || entities.isEmpty()) {

            return responses;
        }

        for (VerificationAuditLog entity : entities) {

            VerificationAuditLogResponse response =
                    toResponse(
                            entity
                    );

            if (response != null) {
                responses.add(
                        response
                );
            }
        }

        return responses;
    }

    private void validatePositiveId(
            Long value,
            String fieldName) {

        if (value == null
                || value <= 0L) {

            throw new InvalidVerificationRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }
}