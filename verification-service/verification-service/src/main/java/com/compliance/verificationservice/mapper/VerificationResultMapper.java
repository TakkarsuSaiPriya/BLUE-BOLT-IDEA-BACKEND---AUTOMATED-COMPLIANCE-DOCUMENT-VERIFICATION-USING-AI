package com.compliance.verificationservice.mapper;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.response.RuleResultResponse;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.VerificationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class VerificationResultMapper {

    public VerificationResult createVerificationResult(
            OcrResultInternalResponse ocrResult,
            String documentType,
            String requestedBy) {

        if (ocrResult == null) {
            throw new IllegalArgumentException(
                    "OCR result cannot be null"
            );
        }

        if (ocrResult.getId() == null
                || ocrResult.getId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid OCR result ID is required"
            );
        }

        if (ocrResult.getDocumentId() == null
                || ocrResult.getDocumentId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid document ID is required"
            );
        }

        VerificationResult verificationResult =
                new VerificationResult();

        verificationResult.setDocumentId(
                ocrResult.getDocumentId()
        );

        verificationResult.setOcrResultId(
                ocrResult.getId()
        );

        verificationResult.setOriginalFileName(
                ocrResult.getOriginalFileName()
        );

        verificationResult.setDocumentType(
                documentType
        );

        verificationResult.setStatus(
                VerificationStatus.PENDING
        );

        verificationResult.setOcrConfidence(
                ocrResult.getAverageConfidence()
        );

        verificationResult.setRequestedBy(
                normalizeUsername(
                        requestedBy
                )
        );

        verificationResult.setRetryCount(0);
        verificationResult.setTotalRuleCount(0);
        verificationResult.setPassedRuleCount(0);
        verificationResult.setFailedRuleCount(0);
        verificationResult.setWarningRuleCount(0);
        verificationResult.setSkippedRuleCount(0);
        verificationResult.setMandatoryFailureCount(0);

        return verificationResult;
    }

    public VerificationResultResponse toResponse(
            VerificationResult entity) {

        if (entity == null) {
            return null;
        }

        VerificationResultResponse response =
                new VerificationResultResponse();

        response.setId(
                entity.getId()
        );

        response.setDocumentId(
                entity.getDocumentId()
        );

        response.setOcrResultId(
                entity.getOcrResultId()
        );

        response.setOriginalFileName(
                entity.getOriginalFileName()
        );

        response.setDocumentType(
                entity.getDocumentType()
        );

        response.setStatus(
                entity.getStatus()
        );

        response.setVerificationScore(
                entity.getVerificationScore()
        );

        response.setOcrConfidence(
                entity.getOcrConfidence()
        );

        response.setTotalRuleCount(
                entity.getTotalRuleCount()
        );

        response.setPassedRuleCount(
                entity.getPassedRuleCount()
        );

        response.setFailedRuleCount(
                entity.getFailedRuleCount()
        );

        response.setWarningRuleCount(
                entity.getWarningRuleCount()
        );

        response.setSkippedRuleCount(
                entity.getSkippedRuleCount()
        );

        response.setMandatoryFailureCount(
                entity.getMandatoryFailureCount()
        );

        response.setSummary(
                entity.getSummary()
        );

        response.setFailureReason(
                entity.getFailureReason()
        );

        response.setRequestedBy(
                entity.getRequestedBy()
        );

        response.setRetryCount(
                entity.getRetryCount()
        );

        response.setProcessingDurationMs(
                entity.getProcessingDurationMs()
        );

        response.setStartedAt(
                entity.getStartedAt()
        );

        response.setCompletedAt(
                entity.getCompletedAt()
        );

        response.setReviewDecision(
                entity.getReviewDecision()
        );

        response.setReviewedBy(
                entity.getReviewedBy()
        );

        response.setReviewReason(
                entity.getReviewReason()
        );

        response.setReviewedAt(
                entity.getReviewedAt()
        );

        response.setCreatedAt(
                entity.getCreatedAt()
        );

        response.setUpdatedAt(
                entity.getUpdatedAt()
        );

        response.setRuleResults(
                toRuleResponses(
                        entity.getRuleResults()
                )
        );

        return response;
    }

    public RuleResultResponse toRuleResponse(
            VerificationRuleResult entity) {

        if (entity == null) {
            return null;
        }

        RuleResultResponse response =
                new RuleResultResponse();

        response.setId(
                entity.getId()
        );

        response.setRuleCode(
                entity.getRuleCode()
        );

        response.setRuleName(
                entity.getRuleName()
        );

        response.setRuleDescription(
                entity.getRuleDescription()
        );

        response.setCategory(
                entity.getCategory()
        );

        response.setOutcome(
                entity.getOutcome()
        );

        response.setMandatory(
                entity.isMandatory()
        );

        response.setWeight(
                entity.getWeight()
        );

        response.setScoreAwarded(
                entity.getScoreAwarded()
        );

        response.setMessage(
                entity.getMessage()
        );

        response.setExpectedValue(
                entity.getExpectedValue()
        );

        response.setActualValue(
                entity.getActualValue()
        );

        response.setExecutionOrder(
                entity.getExecutionOrder()
        );

        response.setExecutionDurationMs(
                entity.getExecutionDurationMs()
        );

        return response;
    }

    public List<VerificationResultResponse>
    toResponseList(
            List<VerificationResult> entities) {

        if (entities == null
                || entities.isEmpty()) {

            return new ArrayList<>();
        }

        List<VerificationResultResponse> responses =
                new ArrayList<>();

        for (VerificationResult entity : entities) {

            VerificationResultResponse response =
                    toResponse(entity);

            if (response != null) {
                responses.add(response);
            }
        }

        return responses;
    }

    private List<RuleResultResponse>
    toRuleResponses(
            List<VerificationRuleResult> entities) {

        if (entities == null
                || entities.isEmpty()) {

            return new ArrayList<>();
        }

        List<RuleResultResponse> responses =
                new ArrayList<>();

        for (VerificationRuleResult entity : entities) {

            RuleResultResponse response =
                    toRuleResponse(entity);

            if (response != null) {
                responses.add(response);
            }
        }

        return responses;
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "system";
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }
}