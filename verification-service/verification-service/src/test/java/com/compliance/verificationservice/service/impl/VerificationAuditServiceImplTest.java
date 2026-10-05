package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.dto.response.VerificationAuditLogResponse;
import com.compliance.verificationservice.entity.VerificationAuditLog;
import com.compliance.verificationservice.enums.VerificationAuditAction;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.repository.VerificationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationAuditServiceImplTest {

    @Mock
    private VerificationAuditLogRepository
            repository;

    private VerificationAuditServiceImpl service;

    @BeforeEach
    void setUp() {

        service =
                new VerificationAuditServiceImpl(
                        repository
                );
    }

    @Test
    @DisplayName(
            "Should save a verification audit record"
    )
    void shouldSaveAuditRecord() {

        service.recordAudit(
                10L,
                1L,
                2L,
                VerificationAuditAction
                        .VERIFICATION_COMPLETED,
                "SUCCESS",
                "TestUser",
                "Verification completed",
                "correlation-100",
                "127.0.0.1",
                "/api/verifications/manual",
                25L
        );

        ArgumentCaptor<VerificationAuditLog> captor =
                ArgumentCaptor.forClass(
                        VerificationAuditLog.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        VerificationAuditLog saved =
                captor.getValue();

        assertThat(saved.getVerificationResultId())
                .isEqualTo(10L);

        assertThat(saved.getDocumentId())
                .isEqualTo(1L);

        assertThat(saved.getOcrResultId())
                .isEqualTo(2L);

        assertThat(saved.getAction())
                .isEqualTo(
                        VerificationAuditAction
                                .VERIFICATION_COMPLETED
                );

        assertThat(saved.getExecutionStatus())
                .isEqualTo(
                        "SUCCESS"
                );

        assertThat(saved.getUsername())
                .isEqualTo(
                        "testuser"
                );

        assertThat(saved.getCorrelationId())
                .isEqualTo(
                        "correlation-100"
                );
    }

    @Test
    @DisplayName(
            "Should reject audit record without action"
    )
    void shouldRejectAuditWithoutAction() {

        assertThatThrownBy(() ->
                service.recordAudit(
                        10L,
                        1L,
                        2L,
                        null,
                        "SUCCESS",
                        "testuser",
                        "details",
                        "correlation",
                        null,
                        null,
                        1L
                )
        )
                .isInstanceOf(
                        InvalidVerificationRequestException.class
                )
                .hasMessageContaining(
                        "Audit action"
                );
    }

    @Test
    @DisplayName(
            "Should map document audit logs to responses"
    )
    void shouldReturnDocumentAuditLogs() {

        VerificationAuditLog auditLog =
                createAuditLog();

        when(
                repository
                        .findAllByDocumentIdOrderByCreatedAtDesc(
                                1L
                        )
        ).thenReturn(
                List.of(
                        auditLog
                )
        );

        List<VerificationAuditLogResponse> responses =
                service.getAuditLogsByDocumentId(
                        1L
                );

        assertThat(responses)
                .hasSize(1);

        assertThat(responses.get(0)
                .getDocumentId())
                .isEqualTo(1L);

        assertThat(responses.get(0)
                .getAction())
                .isEqualTo(
                        VerificationAuditAction
                                .VERIFICATION_COMPLETED
                );
    }

    @Test
    @DisplayName(
            "Should normalize username before querying audit logs"
    )
    void shouldNormalizeUsername() {

        when(
                repository
                        .findAllByUsernameOrderByCreatedAtDesc(
                                "reviewerone"
                        )
        ).thenReturn(
                List.of()
        );

        service.getAuditLogsByUsername(
                " ReviewerOne "
        );

        verify(
                repository
        ).findAllByUsernameOrderByCreatedAtDesc(
                "reviewerone"
        );
    }

    @Test
    @DisplayName(
            "Should reject invalid document ID"
    )
    void shouldRejectInvalidDocumentId() {

        assertThatThrownBy(() ->
                service.getAuditLogsByDocumentId(
                        0L
                )
        )
                .isInstanceOf(
                        InvalidVerificationRequestException.class
                )
                .hasMessageContaining(
                        "Document ID"
                );
    }

    private VerificationAuditLog createAuditLog() {

        VerificationAuditLog auditLog =
                new VerificationAuditLog();

        auditLog.setId(
                100L
        );

        auditLog.setVerificationResultId(
                10L
        );

        auditLog.setDocumentId(
                1L
        );

        auditLog.setOcrResultId(
                2L
        );

        auditLog.setAction(
                VerificationAuditAction
                        .VERIFICATION_COMPLETED
        );

        auditLog.setExecutionStatus(
                "SUCCESS"
        );

        auditLog.setUsername(
                "testuser"
        );

        auditLog.setDetails(
                "Verification completed"
        );

        auditLog.setCorrelationId(
                "correlation-100"
        );

        auditLog.setProcessingDurationMs(
                25L
        );

        return auditLog;
    }
}