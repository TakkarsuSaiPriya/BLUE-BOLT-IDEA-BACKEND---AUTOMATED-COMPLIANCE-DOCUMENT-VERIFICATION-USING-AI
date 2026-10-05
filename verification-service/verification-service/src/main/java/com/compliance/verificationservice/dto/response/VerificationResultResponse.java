package com.compliance.verificationservice.dto.response;

import com.compliance.verificationservice.enums.ReviewDecision;
import com.compliance.verificationservice.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VerificationResultResponse {

    private Long id;
    private Long documentId;
    private Long ocrResultId;
    private String originalFileName;
    private String documentType;
    private VerificationStatus status;
    private BigDecimal verificationScore;
    private BigDecimal ocrConfidence;
    private int totalRuleCount;
    private int passedRuleCount;
    private int failedRuleCount;
    private int warningRuleCount;
    private int skippedRuleCount;
    private int mandatoryFailureCount;
    private String summary;
    private String failureReason;
    private String requestedBy;
    private int retryCount;
    private Long processingDurationMs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private ReviewDecision reviewDecision;
    private String reviewedBy;
    private String reviewReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RuleResultResponse> ruleResults =
            new ArrayList<>();

    public VerificationResultResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId = ocrResultId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName =
                originalFileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(
            String documentType) {

        this.documentType = documentType;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(
            VerificationStatus status) {

        this.status = status;
    }

    public BigDecimal getVerificationScore() {
        return verificationScore;
    }

    public void setVerificationScore(
            BigDecimal verificationScore) {

        this.verificationScore =
                verificationScore;
    }

    public BigDecimal getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(
            BigDecimal ocrConfidence) {

        this.ocrConfidence =
                ocrConfidence;
    }

    public int getTotalRuleCount() {
        return totalRuleCount;
    }

    public void setTotalRuleCount(
            int totalRuleCount) {

        this.totalRuleCount =
                totalRuleCount;
    }

    public int getPassedRuleCount() {
        return passedRuleCount;
    }

    public void setPassedRuleCount(
            int passedRuleCount) {

        this.passedRuleCount =
                passedRuleCount;
    }

    public int getFailedRuleCount() {
        return failedRuleCount;
    }

    public void setFailedRuleCount(
            int failedRuleCount) {

        this.failedRuleCount =
                failedRuleCount;
    }

    public int getWarningRuleCount() {
        return warningRuleCount;
    }

    public void setWarningRuleCount(
            int warningRuleCount) {

        this.warningRuleCount =
                warningRuleCount;
    }

    public int getSkippedRuleCount() {
        return skippedRuleCount;
    }

    public void setSkippedRuleCount(
            int skippedRuleCount) {

        this.skippedRuleCount =
                skippedRuleCount;
    }

    public int getMandatoryFailureCount() {
        return mandatoryFailureCount;
    }

    public void setMandatoryFailureCount(
            int mandatoryFailureCount) {

        this.mandatoryFailureCount =
                mandatoryFailureCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(
            String summary) {

        this.summary = summary;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(
            String failureReason) {

        this.failureReason =
                failureReason;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(
            String requestedBy) {

        this.requestedBy =
                requestedBy;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(
            int retryCount) {

        this.retryCount = retryCount;
    }

    public Long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(
            Long processingDurationMs) {

        this.processingDurationMs =
                processingDurationMs;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(
            LocalDateTime startedAt) {

        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }

    public ReviewDecision getReviewDecision() {
        return reviewDecision;
    }

    public void setReviewDecision(
            ReviewDecision reviewDecision) {

        this.reviewDecision =
                reviewDecision;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(
            String reviewedBy) {

        this.reviewedBy = reviewedBy;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(
            String reviewReason) {

        this.reviewReason = reviewReason;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(
            LocalDateTime reviewedAt) {

        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }

    public List<RuleResultResponse> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(
            List<RuleResultResponse> ruleResults) {

        this.ruleResults =
                ruleResults == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        ruleResults
                );
    }
}