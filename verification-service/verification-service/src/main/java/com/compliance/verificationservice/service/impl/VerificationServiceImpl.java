package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.dto.response.VerificationSummaryResponse;
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
import com.compliance.verificationservice.service.interfaces.VerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class VerificationServiceImpl
        implements VerificationService {

    private final VerificationResultRepository
            verificationResultRepository;

    private final VerificationResultMapper
            verificationResultMapper;

    private final VerificationRuleEngine
            verificationRuleEngine;

    private final VerificationProperties
            verificationProperties;

    public VerificationServiceImpl(
            VerificationResultRepository
                    verificationResultRepository,
            VerificationResultMapper
                    verificationResultMapper,
            VerificationRuleEngine
                    verificationRuleEngine,
            VerificationProperties
                    verificationProperties) {

        this.verificationResultRepository =
                verificationResultRepository;

        this.verificationResultMapper =
                verificationResultMapper;

        this.verificationRuleEngine =
                verificationRuleEngine;

        this.verificationProperties =
                verificationProperties;
    }

    @Override
    @Transactional
    public VerificationResultResponse
    processVerification(
            OcrResultInternalResponse ocrResult,
            String documentType,
            String requestedBy,
            boolean forceReprocess) {

        validateOcrResult(
                ocrResult
        );

        String normalizedUsername =
                normalizeUsername(
                        requestedBy
                );

        boolean existingResultPresent =
                verificationResultRepository
                        .existsByOcrResultIdAndActiveTrue(
                                ocrResult.getId()
                        );

        if (existingResultPresent
                && !forceReprocess) {

            throw VerificationAlreadyExistsException
                    .forOcrResult(
                            ocrResult.getId()
                    );
        }

        VerificationResult verificationResult =
                verificationResultMapper
                        .createVerificationResult(
                                ocrResult,
                                normalizeDocumentType(
                                        documentType
                                ),
                                normalizedUsername
                        );

        verificationResult.setStatus(
                VerificationStatus.PENDING
        );

        VerificationContext context =
                VerificationContext.fromOcrResult(
                        ocrResult,
                        documentType,
                        normalizedUsername
                );

        VerificationResult processedResult =
                verificationRuleEngine.execute(
                        verificationResult,
                        context
                );

        VerificationResult savedResult =
                verificationResultRepository.save(
                        processedResult
                );

        return verificationResultMapper.toResponse(
                savedResult
        );
    }

    @Override
    @Transactional
    public VerificationResultResponse
    retryVerification(
            Long verificationResultId,
            OcrResultInternalResponse ocrResult,
            String requestedBy,
            String reason) {

        validatePositiveId(
                verificationResultId,
                "Verification result ID"
        );

        validateOcrResult(
                ocrResult
        );

        validateRetryReason(
                reason
        );

        VerificationResult existingResult =
                findEntityById(
                        verificationResultId
                );

        validateRetryAllowed(
                existingResult
        );

        validateRetryOcrResult(
                existingResult,
                ocrResult
        );

        int nextRetryCount =
                existingResult.getRetryCount() + 1;

        if (nextRetryCount
                > verificationProperties
                .getMaximumRetryCount()) {

            throw new InvalidVerificationRequestException(
                    "Maximum verification retry count of "
                            + verificationProperties
                            .getMaximumRetryCount()
                            + " has been reached"
            );
        }

        String normalizedUsername =
                normalizeUsername(
                        requestedBy
                );

        VerificationResult retryResult =
                verificationResultMapper
                        .createVerificationResult(
                                ocrResult,
                                existingResult
                                        .getDocumentType(),
                                normalizedUsername
                        );

        retryResult.setRetryCount(
                nextRetryCount
        );

        retryResult.setStatus(
                VerificationStatus.PENDING
        );

        retryResult.setSummary(
                "Verification retry requested: "
                        + normalizeLimitedText(
                        reason,
                        900
                )
        );

        VerificationContext context =
                VerificationContext.fromOcrResult(
                        ocrResult,
                        existingResult
                                .getDocumentType(),
                        normalizedUsername
                );

        VerificationResult processedRetry =
                verificationRuleEngine.execute(
                        retryResult,
                        context
                );

        VerificationResult savedRetry =
                verificationResultRepository.save(
                        processedRetry
                );

        return verificationResultMapper.toResponse(
                savedRetry
        );
    }

    @Override
    @Transactional
    public VerificationResultResponse
    reviewVerification(
            Long verificationResultId,
            ReviewDecisionRequest request,
            String reviewedBy) {

        validatePositiveId(
                verificationResultId,
                "Verification result ID"
        );

        validateReviewRequest(
                request
        );

        VerificationResult verificationResult =
                findEntityById(
                        verificationResultId
                );

        if (verificationResult.getStatus()
                != VerificationStatus.REVIEW_REQUIRED) {

            throw new InvalidVerificationRequestException(
                    "Manual review is allowed only for "
                            + "REVIEW_REQUIRED verification results"
            );
        }

        String normalizedReviewer =
                normalizeUsername(
                        reviewedBy
                );

        String normalizedReason =
                normalizeLimitedText(
                        request.getReason(),
                        1000
                );

        validateReviewReason(
                request.getDecision(),
                normalizedReason
        );

        verificationResult.setReviewDecision(
                request.getDecision()
        );

        verificationResult.setReviewedBy(
                normalizedReviewer
        );

        verificationResult.setReviewReason(
                normalizedReason
        );

        verificationResult.setReviewedAt(
                LocalDateTime.now()
        );

        applyReviewDecision(
                verificationResult,
                request.getDecision()
        );

        verificationResult.setSummary(
                buildReviewSummary(
                        request.getDecision(),
                        normalizedReviewer,
                        normalizedReason
                )
        );

        VerificationResult savedResult =
                verificationResultRepository.save(
                        verificationResult
                );

        return verificationResultMapper.toResponse(
                savedResult
        );
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationResultResponse
    getVerificationResult(
            Long verificationResultId) {

        validatePositiveId(
                verificationResultId,
                "Verification result ID"
        );

        VerificationResult result =
                verificationResultRepository
                        .findWithRuleResultsByIdAndActiveTrue(
                                verificationResultId
                        )
                        .orElseThrow(() ->
                                new VerificationNotFoundException(
                                        verificationResultId
                                )
                        );

        return verificationResultMapper.toResponse(
                result
        );
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationResultResponse
    getLatestByDocumentId(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        VerificationResult result =
                verificationResultRepository
                        .findFirstWithRuleResultsByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
                                documentId
                        )
                        .orElseThrow(() ->
                                new VerificationNotFoundException(
                                        "No verification result found "
                                                + "for document ID: "
                                                + documentId
                                )
                        );

        return verificationResultMapper.toResponse(
                result
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResultResponse>
    getDocumentHistory(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        List<VerificationResult> results =
                verificationResultRepository
                        .findAllByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
                                documentId
                        );

        return verificationResultMapper
                .toResponseList(
                        results
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResultResponse>
    getResultsForUser(
            String username) {

        String normalizedUsername =
                normalizeUsername(
                        username
                );

        List<VerificationResult> results =
                verificationResultRepository
                        .findAllByRequestedByAndActiveTrueOrderByCreatedAtDesc(
                                normalizedUsername
                        );

        return verificationResultMapper
                .toResponseList(
                        results
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResultResponse>
    getResultsByStatus(
            VerificationStatus status) {

        if (status == null) {
            throw new InvalidVerificationRequestException(
                    "Verification status is required"
            );
        }

        List<VerificationResult> results =
                verificationResultRepository
                        .findAllByStatusAndActiveTrueOrderByCreatedAtDesc(
                                status
                        );

        return verificationResultMapper
                .toResponseList(
                        results
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResultResponse>
    getAllResults() {

        List<VerificationResult> results =
                verificationResultRepository
                        .findAllByActiveTrueOrderByCreatedAtDesc();

        return verificationResultMapper
                .toResponseList(
                        results
                );
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationSummaryResponse
    getStatusSummary() {

        VerificationSummaryResponse summary =
                new VerificationSummaryResponse();

        summary.setTotal(
                verificationResultRepository
                        .countByActiveTrue()
        );

        summary.setPending(
                countByStatus(
                        VerificationStatus.PENDING
                )
        );

        summary.setProcessing(
                countByStatus(
                        VerificationStatus.PROCESSING
                )
        );

        summary.setVerified(
                countByStatus(
                        VerificationStatus.VERIFIED
                )
        );

        summary.setRejected(
                countByStatus(
                        VerificationStatus.REJECTED
                )
        );

        summary.setReviewRequired(
                countByStatus(
                        VerificationStatus.REVIEW_REQUIRED
                )
        );

        summary.setFailed(
                countByStatus(
                        VerificationStatus.FAILED
                )
        );

        return summary;
    }

    private VerificationResult findEntityById(
            Long verificationResultId) {

        return verificationResultRepository
                .findWithRuleResultsByIdAndActiveTrue(
                        verificationResultId
                )
                .orElseThrow(() ->
                        new VerificationNotFoundException(
                                verificationResultId
                        )
                );
    }

    private long countByStatus(
            VerificationStatus status) {

        return verificationResultRepository
                .countByStatusAndActiveTrue(
                        status
                );
    }

    private void validateOcrResult(
            OcrResultInternalResponse ocrResult) {

        if (ocrResult == null) {
            throw new InvalidVerificationRequestException(
                    "OCR result is required"
            );
        }

        validatePositiveId(
                ocrResult.getId(),
                "OCR result ID"
        );

        validatePositiveId(
                ocrResult.getDocumentId(),
                "Document ID"
        );

        if (ocrResult.getStatus() == null
                || ocrResult.getStatus().isBlank()) {

            throw new InvalidVerificationRequestException(
                    "OCR status is required"
            );
        }

        String normalizedStatus =
                ocrResult.getStatus()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!"COMPLETED".equals(
                normalizedStatus)
                && !"LOW_CONFIDENCE".equals(
                normalizedStatus)) {

            throw new InvalidVerificationRequestException(
                    "OCR result must have COMPLETED or "
                            + "LOW_CONFIDENCE status. Current status: "
                            + normalizedStatus
            );
        }
    }

    private void validateRetryAllowed(
            VerificationResult existingResult) {

        VerificationStatus status =
                existingResult.getStatus();

        if (status == VerificationStatus.PROCESSING
                || status == VerificationStatus.PENDING) {

            throw new InvalidVerificationRequestException(
                    "Verification cannot be retried while its "
                            + "status is "
                            + status
            );
        }

        if (status == VerificationStatus.VERIFIED) {

            throw new InvalidVerificationRequestException(
                    "A verified result cannot be retried"
            );
        }
    }

    private void validateRetryOcrResult(
            VerificationResult existingResult,
            OcrResultInternalResponse ocrResult) {

        if (!existingResult.getDocumentId()
                .equals(
                        ocrResult.getDocumentId()
                )) {

            throw new InvalidVerificationRequestException(
                    "OCR result document ID does not match "
                            + "the existing verification document ID"
            );
        }
    }

    private void validateRetryReason(
            String reason) {

        if (reason == null
                || reason.isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Retry reason is required"
            );
        }

        if (reason.trim().length() > 1000) {

            throw new InvalidVerificationRequestException(
                    "Retry reason cannot exceed 1000 characters"
            );
        }
    }

    private void validateReviewRequest(
            ReviewDecisionRequest request) {

        if (request == null) {
            throw new InvalidVerificationRequestException(
                    "Review request is required"
            );
        }

        if (request.getDecision() == null) {
            throw new InvalidVerificationRequestException(
                    "Review decision is required"
            );
        }
    }

    private void validateReviewReason(
            ReviewDecision decision,
            String reason) {

        if ((decision == ReviewDecision.REJECTED
                || decision
                == ReviewDecision.RETURNED_FOR_REPROCESSING)
                && (reason == null
                || reason.isBlank())) {

            throw new InvalidVerificationRequestException(
                    "Review reason is required for decision: "
                            + decision
            );
        }
    }

    private void applyReviewDecision(
            VerificationResult verificationResult,
            ReviewDecision decision) {

        switch (decision) {

            case APPROVED ->
                    verificationResult.setStatus(
                            VerificationStatus.VERIFIED
                    );

            case REJECTED ->
                    verificationResult.setStatus(
                            VerificationStatus.REJECTED
                    );

            case RETURNED_FOR_REPROCESSING ->
                    verificationResult.setStatus(
                            VerificationStatus.REVIEW_REQUIRED
                    );

            default ->
                    throw new InvalidVerificationRequestException(
                            "Unsupported review decision: "
                                    + decision
                    );
        }
    }

    private String buildReviewSummary(
            ReviewDecision decision,
            String reviewedBy,
            String reason) {

        StringBuilder summary =
                new StringBuilder();

        summary.append(
                "Manual review completed with decision "
        );

        summary.append(
                decision
        );

        summary.append(
                " by "
        );

        summary.append(
                reviewedBy
        );

        if (reason != null
                && !reason.isBlank()) {

            summary.append(
                    ". Reason: "
            );

            summary.append(
                    reason
            );
        }

        return normalizeLimitedText(
                summary.toString(),
                1000
        );
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "system";
        }

        String normalizedUsername =
                username.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return normalizeLimitedText(
                normalizedUsername,
                150
        );
    }

    private String normalizeDocumentType(
            String documentType) {

        if (documentType == null
                || documentType.isBlank()) {

            return "GENERAL";
        }

        return normalizeLimitedText(
                documentType.trim()
                        .toUpperCase(
                                Locale.ROOT
                        ),
                100
        );
    }

    private String normalizeLimitedText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        String normalizedValue =
                value.trim();

        if (normalizedValue.length()
                > maximumLength) {

            return normalizedValue.substring(
                    0,
                    maximumLength
            );
        }

        return normalizedValue;
    }

    private void validatePositiveId(
            Long value,
            String fieldName) {

        if (value == null
                || value <= 0) {

            throw new InvalidVerificationRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }
}