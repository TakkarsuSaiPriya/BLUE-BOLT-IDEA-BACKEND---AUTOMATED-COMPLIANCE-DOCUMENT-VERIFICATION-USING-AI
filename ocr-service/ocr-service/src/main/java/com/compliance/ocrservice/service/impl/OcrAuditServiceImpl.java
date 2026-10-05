package com.compliance.ocrservice.service.impl;

import com.compliance.ocrservice.entity.OcrAuditLog;
import com.compliance.ocrservice.enums.OcrAuditAction;
import com.compliance.ocrservice.mapper.OcrAuditMapper;
import com.compliance.ocrservice.repository.OcrAuditLogRepository;
import com.compliance.ocrservice.service.interfaces.OcrAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class OcrAuditServiceImpl
        implements OcrAuditService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OcrAuditServiceImpl.class
            );

    private static final int MAXIMUM_PAYLOAD_LENGTH =
            10000;

    private static final int MAXIMUM_ERROR_LENGTH =
            2000;

    private final OcrAuditLogRepository
            ocrAuditLogRepository;

    private final OcrAuditMapper
            ocrAuditMapper;

    public OcrAuditServiceImpl(
            OcrAuditLogRepository ocrAuditLogRepository,
            OcrAuditMapper ocrAuditMapper) {

        this.ocrAuditLogRepository =
                ocrAuditLogRepository;

        this.ocrAuditMapper =
                ocrAuditMapper;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public OcrAuditLog saveAuditLog(
            OcrAuditLog auditLog) {

        validateAuditLog(auditLog);
        normalizeAuditLog(auditLog);

        return ocrAuditLogRepository.save(
                auditLog
        );
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void recordAudit(
            OcrAuditAction action,
            Long documentId,
            Long ocrResultId,
            String username,
            String executionStatus,
            String requestPayload,
            String responsePayload,
            String errorMessage) {

        try {
            OcrAuditLog auditLog =
                    ocrAuditMapper.createAuditLog(
                            action,
                            documentId,
                            ocrResultId,
                            username,
                            executionStatus,
                            limitPayload(
                                    requestPayload
                            ),
                            limitPayload(
                                    responsePayload
                            ),
                            limitErrorMessage(
                                    errorMessage
                            )
                    );

            normalizeAuditLog(auditLog);

            ocrAuditLogRepository.save(
                    auditLog
            );

        } catch (RuntimeException exception) {

            /*
             * Audit persistence must not cause an OCR
             * API request or message operation to fail.
             */
            LOGGER.error(
                    "Unable to persist OCR audit log. "
                            + "action={}, documentId={}, ocrResultId={}",
                    action,
                    documentId,
                    ocrResultId,
                    exception
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrAuditLog> getByDocumentId(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        return ocrAuditLogRepository
                .findAllByDocumentIdOrderByCreatedAtDesc(
                        documentId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrAuditLog> getByOcrResultId(
            Long ocrResultId) {

        validatePositiveId(
                ocrResultId,
                "OCR result ID"
        );

        return ocrAuditLogRepository
                .findAllByOcrResultIdOrderByCreatedAtDesc(
                        ocrResultId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrAuditLog> getByUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        String normalizedUsername =
                username.trim()
                        .toLowerCase(Locale.ROOT);

        return ocrAuditLogRepository
                .findAllByUsernameOrderByCreatedAtDesc(
                        normalizedUsername
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrAuditLog> getByAction(
            OcrAuditAction action) {

        if (action == null) {
            throw new IllegalArgumentException(
                    "OCR audit action is required"
            );
        }

        return ocrAuditLogRepository
                .findAllByActionOrderByCreatedAtDesc(
                        action
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrAuditLog> getByExecutionStatus(
            String executionStatus) {

        if (executionStatus == null
                || executionStatus.isBlank()) {

            throw new IllegalArgumentException(
                    "Execution status is required"
            );
        }

        String normalizedExecutionStatus =
                executionStatus.trim()
                        .toUpperCase(Locale.ROOT);

        return ocrAuditLogRepository
                .findAllByExecutionStatusOrderByCreatedAtDesc(
                        normalizedExecutionStatus
                );
    }

    private void validateAuditLog(
            OcrAuditLog auditLog) {

        if (auditLog == null) {
            throw new IllegalArgumentException(
                    "OCR audit log cannot be null"
            );
        }

        if (auditLog.getAction() == null) {
            throw new IllegalArgumentException(
                    "OCR audit action is required"
            );
        }
    }

    private void normalizeAuditLog(
            OcrAuditLog auditLog) {

        validateAuditLog(auditLog);

        if (auditLog.getUsername() == null
                || auditLog.getUsername().isBlank()) {

            auditLog.setUsername("system");

        } else {
            auditLog.setUsername(
                    auditLog.getUsername()
                            .trim()
                            .toLowerCase(Locale.ROOT)
            );
        }

        if (auditLog.getExecutionStatus() == null
                || auditLog.getExecutionStatus().isBlank()) {

            auditLog.setExecutionStatus(
                    "UNKNOWN"
            );

        } else {
            auditLog.setExecutionStatus(
                    auditLog.getExecutionStatus()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        auditLog.setRequestPayload(
                limitPayload(
                        auditLog.getRequestPayload()
                )
        );

        auditLog.setResponsePayload(
                limitPayload(
                        auditLog.getResponsePayload()
                )
        );

        auditLog.setErrorMessage(
                limitErrorMessage(
                        auditLog.getErrorMessage()
                )
        );

        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(
                    LocalDateTime.now()
            );
        }
    }

    private String limitPayload(
            String payload) {

        if (payload == null
                || payload.isBlank()) {

            return null;
        }

        String normalizedPayload =
                payload.trim();

        if (normalizedPayload.length()
                > MAXIMUM_PAYLOAD_LENGTH) {

            return normalizedPayload.substring(
                    0,
                    MAXIMUM_PAYLOAD_LENGTH
            );
        }

        return normalizedPayload;
    }

    private String limitErrorMessage(
            String errorMessage) {

        if (errorMessage == null
                || errorMessage.isBlank()) {

            return null;
        }

        String normalizedMessage =
                errorMessage.trim();

        if (normalizedMessage.length()
                > MAXIMUM_ERROR_LENGTH) {

            return normalizedMessage.substring(
                    0,
                    MAXIMUM_ERROR_LENGTH
            );
        }

        return normalizedMessage;
    }

    private void validatePositiveId(
            Long value,
            String fieldName) {

        if (value == null
                || value <= 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }
}