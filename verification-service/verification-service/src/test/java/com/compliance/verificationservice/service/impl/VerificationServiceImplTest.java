package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.enums.ReviewDecision;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.exception.VerificationAlreadyExistsException;
import com.compliance.verificationservice.exception.VerificationNotFoundException;
import com.compliance.verificationservice.mapper.VerificationResultMapper;
import com.compliance.verificationservice.repository.VerificationResultRepository;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private VerificationResultRepository
            verificationResultRepository;

    @Mock
    private VerificationResultMapper
            verificationResultMapper;

    @Mock
    private VerificationRuleEngine
            verificationRuleEngine;

    @Mock
    private VerificationProperties
            verificationProperties;

    private VerificationServiceImpl
            verificationService;

    @BeforeEach
    void setUp() {

        verificationService =
                new VerificationServiceImpl(
                        verificationResultRepository,
                        verificationResultMapper,
                        verificationRuleEngine,
                        verificationProperties
                );
    }

    @Test
    @DisplayName(
            "Should process and save a new verification"
    )
    void shouldProcessNewVerification() {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        VerificationResult newEntity =
                createVerificationEntity();

        VerificationResult processedEntity =
                createVerificationEntity();

        processedEntity.setStatus(
                VerificationStatus.VERIFIED
        );

        processedEntity.setVerificationScore(
                new BigDecimal("95.00")
        );

        VerificationResultResponse expectedResponse =
                createResponse();

        when(
                verificationResultRepository
                        .existsByOcrResultIdAndActiveTrue(
                                2L
                        )
        ).thenReturn(false);

        when(
                verificationResultMapper
                        .createVerificationResult(
                                eq(ocrResult),
                                eq("GENERAL"),
                                eq("testuser")
                        )
        ).thenReturn(newEntity);

        when(
                verificationRuleEngine.execute(
                        eq(newEntity),
                        any(VerificationContext.class)
                )
        ).thenReturn(processedEntity);

        when(
                verificationResultRepository.save(
                        processedEntity
                )
        ).thenReturn(processedEntity);

        when(
                verificationResultMapper.toResponse(
                        processedEntity
                )
        ).thenReturn(expectedResponse);

        VerificationResultResponse actualResponse =
                verificationService.processVerification(
                        ocrResult,
                        "general",
                        "TestUser",
                        false
                );

        assertThat(actualResponse)
                .isSameAs(
                        expectedResponse
                );

        verify(
                verificationRuleEngine
        ).execute(
                eq(newEntity),
                any(VerificationContext.class)
        );

        verify(
                verificationResultRepository
        ).save(
                processedEntity
        );
    }

    @Test
    @DisplayName(
            "Should reject duplicate OCR verification"
    )
    void shouldRejectDuplicateOcrVerification() {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        when(
                verificationResultRepository
                        .existsByOcrResultIdAndActiveTrue(
                                2L
                        )
        ).thenReturn(true);

        assertThatThrownBy(() ->
                verificationService.processVerification(
                        ocrResult,
                        "GENERAL",
                        "testuser",
                        false
                )
        )
                .isInstanceOf(
                        VerificationAlreadyExistsException.class
                )
                .hasMessageContaining(
                        "OCR result ID: 2"
                );

        verify(
                verificationResultMapper,
                never()
        ).createVerificationResult(
                any(),
                any(),
                any()
        );

        verify(
                verificationResultRepository,
                never()
        ).save(
                any()
        );
    }

    @Test
    @DisplayName(
            "Should allow forced reprocessing"
    )
    void shouldAllowForcedReprocessing() {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        VerificationResult entity =
                createVerificationEntity();

        VerificationResultResponse response =
                createResponse();

        when(
                verificationResultRepository
                        .existsByOcrResultIdAndActiveTrue(
                                2L
                        )
        ).thenReturn(true);

        when(
                verificationResultMapper
                        .createVerificationResult(
                                eq(ocrResult),
                                eq("GENERAL"),
                                eq("testuser")
                        )
        ).thenReturn(entity);

        when(
                verificationRuleEngine.execute(
                        eq(entity),
                        any(VerificationContext.class)
                )
        ).thenReturn(entity);

        when(
                verificationResultRepository.save(
                        entity
                )
        ).thenReturn(entity);

        when(
                verificationResultMapper.toResponse(
                        entity
                )
        ).thenReturn(response);

        VerificationResultResponse result =
                verificationService.processVerification(
                        ocrResult,
                        "GENERAL",
                        "testuser",
                        true
                );

        assertThat(result)
                .isSameAs(
                        response
                );

        verify(
                verificationResultRepository
        ).save(
                entity
        );
    }

    @Test
    @DisplayName(
            "Should return verification result by ID"
    )
    void shouldReturnResultById() {

        VerificationResult entity =
                createVerificationEntity();

        VerificationResultResponse response =
                createResponse();

        when(
                verificationResultRepository
                        .findWithRuleResultsByIdAndActiveTrue(
                                10L
                        )
        ).thenReturn(
                Optional.of(
                        entity
                )
        );

        when(
                verificationResultMapper.toResponse(
                        entity
                )
        ).thenReturn(response);

        VerificationResultResponse result =
                verificationService.getVerificationResult(
                        10L
                );

        assertThat(result)
                .isSameAs(
                        response
                );
    }

    @Test
    @DisplayName(
            "Should throw not-found exception for unknown result"
    )
    void shouldThrowForUnknownVerificationResult() {

        when(
                verificationResultRepository
                        .findWithRuleResultsByIdAndActiveTrue(
                                999L
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                verificationService.getVerificationResult(
                        999L
                )
        )
                .isInstanceOf(
                        VerificationNotFoundException.class
                )
                .hasMessageContaining(
                        "999"
                );
    }

    @Test
    @DisplayName(
            "Should approve a review-required result"
    )
    void shouldApproveReviewRequiredResult() {

        VerificationResult entity =
                createVerificationEntity();

        entity.setId(
                10L
        );

        entity.setStatus(
                VerificationStatus.REVIEW_REQUIRED
        );

        ReviewDecisionRequest request =
                new ReviewDecisionRequest();

        request.setDecision(
                ReviewDecision.APPROVED
        );

        request.setReason(
                "Manually verified"
        );

        VerificationResultResponse response =
                createResponse();

        response.setStatus(
                VerificationStatus.VERIFIED
        );

        response.setReviewDecision(
                ReviewDecision.APPROVED
        );

        when(
                verificationResultRepository
                        .findWithRuleResultsByIdAndActiveTrue(
                                10L
                        )
        ).thenReturn(
                Optional.of(
                        entity
                )
        );

        when(
                verificationResultRepository.save(
                        any(VerificationResult.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                verificationResultMapper.toResponse(
                        any(VerificationResult.class)
                )
        ).thenReturn(response);

        VerificationResultResponse result =
                verificationService.reviewVerification(
                        10L,
                        request,
                        "ReviewerOne"
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.VERIFIED
                );

        ArgumentCaptor<VerificationResult> captor =
                ArgumentCaptor.forClass(
                        VerificationResult.class
                );

        verify(
                verificationResultRepository
        ).save(
                captor.capture()
        );

        assertThat(
                captor.getValue().getStatus()
        ).isEqualTo(
                VerificationStatus.VERIFIED
        );

        assertThat(
                captor.getValue().getReviewedBy()
        ).isEqualTo(
                "reviewerone"
        );
    }

    @Test
    @DisplayName(
            "Should reject review when status is not review-required"
    )
    void shouldRejectReviewForVerifiedResult() {

        VerificationResult entity =
                createVerificationEntity();

        entity.setStatus(
                VerificationStatus.VERIFIED
        );

        ReviewDecisionRequest request =
                new ReviewDecisionRequest();

        request.setDecision(
                ReviewDecision.APPROVED
        );

        when(
                verificationResultRepository
                        .findWithRuleResultsByIdAndActiveTrue(
                                10L
                        )
        ).thenReturn(
                Optional.of(
                        entity
                )
        );

        assertThatThrownBy(() ->
                verificationService.reviewVerification(
                        10L,
                        request,
                        "reviewer"
                )
        )
                .isInstanceOf(
                        InvalidVerificationRequestException.class
                )
                .hasMessageContaining(
                        "REVIEW_REQUIRED"
                );

        verify(
                verificationResultRepository,
                never()
        ).save(
                any()
        );
    }

    private OcrResultInternalResponse createOcrResult() {

        OcrResultInternalResponse response =
                new OcrResultInternalResponse();

        response.setId(
                2L
        );

        response.setDocumentId(
                1L
        );

        response.setOriginalFileName(
                "document.png"
        );

        response.setStatus(
                "COMPLETED"
        );

        response.setExtractedText(
                "Document Number: REF-1001 "
                        + "Name: Sample User Status: Active"
        );

        response.setAverageConfidence(
                new BigDecimal("90.00")
        );

        response.setPageCount(
                1
        );

        response.setCharacterCount(
                60
        );

        response.setWordCount(
                8
        );

        return response;
    }

    private VerificationResult createVerificationEntity() {

        VerificationResult result =
                new VerificationResult();

        result.setId(
                10L
        );

        result.setDocumentId(
                1L
        );

        result.setOcrResultId(
                2L
        );

        result.setDocumentType(
                "GENERAL"
        );

        result.setRequestedBy(
                "testuser"
        );

        result.setStatus(
                VerificationStatus.PENDING
        );

        return result;
    }

    private VerificationResultResponse createResponse() {

        VerificationResultResponse response =
                new VerificationResultResponse();

        response.setId(
                10L
        );

        response.setDocumentId(
                1L
        );

        response.setOcrResultId(
                2L
        );

        response.setStatus(
                VerificationStatus.VERIFIED
        );

        response.setVerificationScore(
                new BigDecimal("95.00")
        );

        response.setRequestedBy(
                "testuser"
        );

        return response;
    }
}