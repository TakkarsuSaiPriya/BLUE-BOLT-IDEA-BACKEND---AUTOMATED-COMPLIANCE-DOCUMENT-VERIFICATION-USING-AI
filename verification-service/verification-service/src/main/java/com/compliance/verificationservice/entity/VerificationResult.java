package com.compliance.verificationservice.entity;

import com.compliance.verificationservice.enums.ReviewDecision;
import com.compliance.verificationservice.enums.VerificationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "verification_results",
        indexes = {
                @Index(
                        name = "idx_verification_document_id",
                        columnList = "document_id"
                ),
                @Index(
                        name = "idx_verification_ocr_result_id",
                        columnList = "ocr_result_id"
                ),
                @Index(
                        name = "idx_verification_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_verification_requested_by",
                        columnList = "requested_by"
                ),
                @Index(
                        name = "idx_verification_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_verification_document_active",
                        columnList = "document_id, active"
                )
        }
)
public class VerificationResult extends BaseEntity {

    private static final BigDecimal MINIMUM_PERCENTAGE =
            new BigDecimal("0.00");

    private static final BigDecimal MAXIMUM_PERCENTAGE =
            new BigDecimal("100.00");

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "document_id",
            nullable = false
    )
    private Long documentId;

    @Column(
            name = "ocr_result_id",
            nullable = false
    )
    private Long ocrResultId;

    @Column(
            name = "original_file_name",
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "document_type",
            length = 100
    )
    private String documentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private VerificationStatus status;

    @Column(
            name = "verification_score",
            precision = 8,
            scale = 2
    )
    private BigDecimal verificationScore;

    @Column(
            name = "ocr_confidence",
            precision = 8,
            scale = 2
    )
    private BigDecimal ocrConfidence;

    @Column(
            name = "total_rule_count",
            nullable = false
    )
    private int totalRuleCount;

    @Column(
            name = "passed_rule_count",
            nullable = false
    )
    private int passedRuleCount;

    @Column(
            name = "failed_rule_count",
            nullable = false
    )
    private int failedRuleCount;

    @Column(
            name = "warning_rule_count",
            nullable = false
    )
    private int warningRuleCount;

    @Column(
            name = "skipped_rule_count",
            nullable = false
    )
    private int skippedRuleCount;

    @Column(
            name = "mandatory_failure_count",
            nullable = false
    )
    private int mandatoryFailureCount;

    @Column(
            name = "summary",
            length = 1000
    )
    private String summary;

    @Column(
            name = "failure_reason",
            length = 2000
    )
    private String failureReason;

    @Column(
            name = "requested_by",
            nullable = false,
            length = 150
    )
    private String requestedBy;

    @Column(
            name = "retry_count",
            nullable = false
    )
    private int retryCount;

    @Column(
            name = "processing_duration_ms"
    )
    private Long processingDurationMs;

    @Column(
            name = "started_at"
    )
    private LocalDateTime startedAt;

    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "review_decision",
            length = 50
    )
    private ReviewDecision reviewDecision;

    @Column(
            name = "reviewed_by",
            length = 150
    )
    private String reviewedBy;

    @Column(
            name = "review_reason",
            length = 1000
    )
    private String reviewReason;

    @Column(
            name = "reviewed_at"
    )
    private LocalDateTime reviewedAt;

    @OneToMany(
            mappedBy = "verificationResult",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("executionOrder ASC, id ASC")
    private List<VerificationRuleResult> ruleResults =
            new ArrayList<>();

    @PrePersist
    protected void initializeVerificationResult() {

        if (status == null) {
            status = VerificationStatus.PENDING;
        }

        requestedBy =
                normalizeUsername(
                        requestedBy
                );

        originalFileName =
                normalizeNullableText(
                        originalFileName,
                        255
                );

        documentType =
                normalizeNullableText(
                        documentType,
                        100
                );

        summary =
                normalizeNullableText(
                        summary,
                        1000
                );

        failureReason =
                normalizeNullableText(
                        failureReason,
                        2000
                );

        reviewedBy =
                normalizeNullableUsername(
                        reviewedBy
                );

        reviewReason =
                normalizeNullableText(
                        reviewReason,
                        1000
                );

        verificationScore =
                normalizePercentage(
                        verificationScore
                );

        ocrConfidence =
                normalizePercentage(
                        ocrConfidence
                );

        retryCount =
                Math.max(
                        retryCount,
                        0
                );

        totalRuleCount =
                Math.max(
                        totalRuleCount,
                        0
                );

        passedRuleCount =
                Math.max(
                        passedRuleCount,
                        0
                );

        failedRuleCount =
                Math.max(
                        failedRuleCount,
                        0
                );

        warningRuleCount =
                Math.max(
                        warningRuleCount,
                        0
                );

        skippedRuleCount =
                Math.max(
                        skippedRuleCount,
                        0
                );

        mandatoryFailureCount =
                Math.max(
                        mandatoryFailureCount,
                        0
                );

        if (processingDurationMs != null) {
            processingDurationMs =
                    Math.max(
                            processingDurationMs,
                            0L
                    );
        }
    }

    public void setVerificationScore(
            BigDecimal verificationScore) {

        this.verificationScore =
                normalizePercentage(
                        verificationScore
                );
    }

    public void setVerificationScore(
            double verificationScore) {

        setVerificationScore(
                BigDecimal.valueOf(
                        verificationScore
                )
        );
    }

    public void setOcrConfidence(
            BigDecimal ocrConfidence) {

        this.ocrConfidence =
                normalizePercentage(
                        ocrConfidence
                );
    }

    public void setOcrConfidence(
            double ocrConfidence) {

        setOcrConfidence(
                BigDecimal.valueOf(
                        ocrConfidence
                )
        );
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName =
                normalizeNullableText(
                        originalFileName,
                        255
                );
    }

    public void setDocumentType(
            String documentType) {

        this.documentType =
                normalizeNullableText(
                        documentType,
                        100
                );
    }

    public void setSummary(
            String summary) {

        this.summary =
                normalizeNullableText(
                        summary,
                        1000
                );
    }

    public void setFailureReason(
            String failureReason) {

        this.failureReason =
                normalizeNullableText(
                        failureReason,
                        2000
                );
    }

    public void setRequestedBy(
            String requestedBy) {

        this.requestedBy =
                normalizeUsername(
                        requestedBy
                );
    }

    public void setReviewedBy(
            String reviewedBy) {

        this.reviewedBy =
                normalizeNullableUsername(
                        reviewedBy
                );
    }

    public void setReviewReason(
            String reviewReason) {

        this.reviewReason =
                normalizeNullableText(
                        reviewReason,
                        1000
                );
    }

    public void setRetryCount(
            int retryCount) {

        this.retryCount =
                Math.max(
                        retryCount,
                        0
                );
    }

    public void setTotalRuleCount(
            int totalRuleCount) {

        this.totalRuleCount =
                Math.max(
                        totalRuleCount,
                        0
                );
    }

    public void setPassedRuleCount(
            int passedRuleCount) {

        this.passedRuleCount =
                Math.max(
                        passedRuleCount,
                        0
                );
    }

    public void setFailedRuleCount(
            int failedRuleCount) {

        this.failedRuleCount =
                Math.max(
                        failedRuleCount,
                        0
                );
    }

    public void setWarningRuleCount(
            int warningRuleCount) {

        this.warningRuleCount =
                Math.max(
                        warningRuleCount,
                        0
                );
    }

    public void setSkippedRuleCount(
            int skippedRuleCount) {

        this.skippedRuleCount =
                Math.max(
                        skippedRuleCount,
                        0
                );
    }

    public void setMandatoryFailureCount(
            int mandatoryFailureCount) {

        this.mandatoryFailureCount =
                Math.max(
                        mandatoryFailureCount,
                        0
                );
    }

    public void setProcessingDurationMs(
            Long processingDurationMs) {

        if (processingDurationMs == null) {
            this.processingDurationMs = null;
            return;
        }

        this.processingDurationMs =
                Math.max(
                        processingDurationMs,
                        0L
                );
    }

    public void addRuleResult(
            VerificationRuleResult ruleResult) {

        if (ruleResult == null) {
            return;
        }

        if (!ruleResults.contains(ruleResult)) {
            ruleResults.add(ruleResult);
        }

        ruleResult.setVerificationResult(
                this
        );
    }

    public void removeRuleResult(
            VerificationRuleResult ruleResult) {

        if (ruleResult == null) {
            return;
        }

        ruleResults.remove(ruleResult);

        if (ruleResult.getVerificationResult() == this) {
            ruleResult.setVerificationResult(
                    null
            );
        }
    }

    public void clearRuleResults() {

        List<VerificationRuleResult> existingResults =
                new ArrayList<>(
                        ruleResults
                );

        for (VerificationRuleResult ruleResult
                : existingResults) {

            removeRuleResult(
                    ruleResult
            );
        }
    }

    public void setRuleResults(
            List<VerificationRuleResult> ruleResults) {

        clearRuleResults();

        if (ruleResults == null) {
            return;
        }

        for (VerificationRuleResult ruleResult
                : ruleResults) {

            addRuleResult(
                    ruleResult
            );
        }
    }

    public boolean isTerminalStatus() {

        return status == VerificationStatus.VERIFIED
                || status == VerificationStatus.REJECTED
                || status == VerificationStatus.REVIEW_REQUIRED
                || status == VerificationStatus.FAILED;
    }

    public boolean requiresManualReview() {

        return status
                == VerificationStatus.REVIEW_REQUIRED;
    }

    public boolean isSuccessful() {

        return status
                == VerificationStatus.VERIFIED;
    }

    private BigDecimal normalizePercentage(
            BigDecimal value) {

        if (value == null) {
            return null;
        }

        BigDecimal normalizedValue =
                value.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        if (normalizedValue.compareTo(
                MINIMUM_PERCENTAGE) < 0) {

            return MINIMUM_PERCENTAGE;
        }

        if (normalizedValue.compareTo(
                MAXIMUM_PERCENTAGE) > 0) {

            return MAXIMUM_PERCENTAGE;
        }

        return normalizedValue;
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

        return limitText(
                normalizedUsername,
                150
        );
    }

    private String normalizeNullableUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return null;
        }

        String normalizedUsername =
                username.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return limitText(
                normalizedUsername,
                150
        );
    }

    private String normalizeNullableText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return limitText(
                value.trim(),
                maximumLength
        );
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value == null) {
            return null;
        }

        if (value.length() > maximumLength) {
            return value.substring(
                    0,
                    maximumLength
            );
        }

        return value;
    }
}