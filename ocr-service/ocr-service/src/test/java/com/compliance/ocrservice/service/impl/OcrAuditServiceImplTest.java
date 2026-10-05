package com.compliance.ocrservice.service.impl;

import com.compliance.ocrservice.entity.OcrAuditLog;
import com.compliance.ocrservice.enums.OcrAuditAction;
import com.compliance.ocrservice.mapper.OcrAuditMapper;
import com.compliance.ocrservice.repository.OcrAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrAuditServiceImplTest {

    @Mock
    private OcrAuditLogRepository
            ocrAuditLogRepository;

    @Mock
    private OcrAuditMapper
            ocrAuditMapper;

    private OcrAuditServiceImpl
            ocrAuditService;

    @BeforeEach
    void setUp() {

        ocrAuditService =
                new OcrAuditServiceImpl(
                        ocrAuditLogRepository,
                        ocrAuditMapper
                );
    }

    @Test
    void saveAuditLogShouldNormalizeAndSaveAuditLog() {

        OcrAuditLog auditLog =
                new OcrAuditLog();

        auditLog.setAction(
                OcrAuditAction.OCR_PROCESS_COMPLETED
        );

        auditLog.setUsername(
                " TestUser "
        );

        auditLog.setExecutionStatus(
                " success "
        );

        when(
                ocrAuditLogRepository.save(
                        any(OcrAuditLog.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        OcrAuditLog result =
                ocrAuditService.saveAuditLog(
                        auditLog
                );

        assertEquals(
                "testuser",
                result.getUsername()
        );

        assertEquals(
                "SUCCESS",
                result.getExecutionStatus()
        );

        assertNotNull(
                result.getCreatedAt()
        );

        verify(
                ocrAuditLogRepository
        ).save(auditLog);
    }

    @Test
    void saveAuditLogShouldUseDefaultsForBlankValues() {

        OcrAuditLog auditLog =
                new OcrAuditLog();

        auditLog.setAction(
                OcrAuditAction.OCR_PROCESS_FAILED
        );

        auditLog.setUsername(" ");
        auditLog.setExecutionStatus(null);

        when(
                ocrAuditLogRepository.save(
                        any(OcrAuditLog.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        OcrAuditLog result =
                ocrAuditService.saveAuditLog(
                        auditLog
                );

        assertEquals(
                "system",
                result.getUsername()
        );

        assertEquals(
                "UNKNOWN",
                result.getExecutionStatus()
        );
    }

    @Test
    void saveAuditLogShouldRejectNullAuditLog() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ocrAuditService
                        .saveAuditLog(null)
        );

        verify(
                ocrAuditLogRepository,
                never()
        ).save(
                any(OcrAuditLog.class)
        );
    }

    @Test
    void saveAuditLogShouldRejectMissingAction() {

        OcrAuditLog auditLog =
                new OcrAuditLog();

        assertThrows(
                IllegalArgumentException.class,
                () -> ocrAuditService
                        .saveAuditLog(
                                auditLog
                        )
        );
    }

    @Test
    void recordAuditShouldCreateAndSaveAuditLog() {

        OcrAuditLog mappedAuditLog =
                new OcrAuditLog();

        mappedAuditLog.setAction(
                OcrAuditAction.OCR_RESULT_VIEWED
        );

        mappedAuditLog.setUsername(
                "testuser"
        );

        mappedAuditLog.setExecutionStatus(
                "SUCCESS"
        );

        when(
                ocrAuditMapper.createAuditLog(
                        OcrAuditAction.OCR_RESULT_VIEWED,
                        20L,
                        30L,
                        "testuser",
                        "SUCCESS",
                        "{\"request\":true}",
                        "{\"response\":true}",
                        null
                )
        ).thenReturn(
                mappedAuditLog
        );

        ocrAuditService.recordAudit(
                OcrAuditAction.OCR_RESULT_VIEWED,
                20L,
                30L,
                "testuser",
                "SUCCESS",
                "{\"request\":true}",
                "{\"response\":true}",
                null
        );

        verify(
                ocrAuditMapper
        ).createAuditLog(
                OcrAuditAction.OCR_RESULT_VIEWED,
                20L,
                30L,
                "testuser",
                "SUCCESS",
                "{\"request\":true}",
                "{\"response\":true}",
                null
        );

        verify(
                ocrAuditLogRepository
        ).save(
                mappedAuditLog
        );
    }

    @Test
    void recordAuditShouldLimitPayloadLength() {

        String longPayload =
                "a".repeat(12000);

        OcrAuditLog mappedAuditLog =
                new OcrAuditLog();

        mappedAuditLog.setAction(
                OcrAuditAction.OCR_RESULTS_LISTED
        );

        mappedAuditLog.setUsername(
                "system"
        );

        mappedAuditLog.setExecutionStatus(
                "SUCCESS"
        );

        when(
                ocrAuditMapper.createAuditLog(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                mappedAuditLog
        );

        ocrAuditService.recordAudit(
                OcrAuditAction.OCR_RESULTS_LISTED,
                null,
                null,
                "system",
                "SUCCESS",
                longPayload,
                longPayload,
                null
        );

        ArgumentCaptor<String> requestCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        ArgumentCaptor<String> responseCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(
                ocrAuditMapper
        ).createAuditLog(
                org.mockito.ArgumentMatchers.eq(
                        OcrAuditAction.OCR_RESULTS_LISTED
                ),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(
                        "system"
                ),
                org.mockito.ArgumentMatchers.eq(
                        "SUCCESS"
                ),
                requestCaptor.capture(),
                responseCaptor.capture(),
                org.mockito.ArgumentMatchers.isNull()
        );

        assertEquals(
                10000,
                requestCaptor
                        .getValue()
                        .length()
        );

        assertEquals(
                10000,
                responseCaptor
                        .getValue()
                        .length()
        );
    }

    @Test
    void getByDocumentIdShouldReturnRepositoryResults() {

        OcrAuditLog auditLog =
                new OcrAuditLog();

        when(
                ocrAuditLogRepository
                        .findAllByDocumentIdOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(
                List.of(
                        auditLog
                )
        );

        List<OcrAuditLog> results =
                ocrAuditService
                        .getByDocumentId(
                                10L
                        );

        assertEquals(
                1,
                results.size()
        );
    }

    @Test
    void getByDocumentIdShouldRejectInvalidId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ocrAuditService
                        .getByDocumentId(
                                0L
                        )
        );
    }

    @Test
    void getByUsernameShouldNormalizeUsername() {

        when(
                ocrAuditLogRepository
                        .findAllByUsernameOrderByCreatedAtDesc(
                                "testuser"
                        )
        ).thenReturn(
                List.of()
        );

        ocrAuditService.getByUsername(
                " TestUser "
        );

        verify(
                ocrAuditLogRepository
        ).findAllByUsernameOrderByCreatedAtDesc(
                "testuser"
        );
    }

    @Test
    void getByExecutionStatusShouldNormalizeStatus() {

        when(
                ocrAuditLogRepository
                        .findAllByExecutionStatusOrderByCreatedAtDesc(
                                "FAILED"
                        )
        ).thenReturn(
                List.of()
        );

        ocrAuditService
                .getByExecutionStatus(
                        " failed "
                );

        verify(
                ocrAuditLogRepository
        ).findAllByExecutionStatusOrderByCreatedAtDesc(
                "FAILED"
        );
    }
}