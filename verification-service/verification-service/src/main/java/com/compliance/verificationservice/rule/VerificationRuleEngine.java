package com.compliance.verificationservice.rule;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.util.VerificationScoreUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class VerificationRuleEngine {

    private final List<VerificationRule> rules;

    private final VerificationScoreUtil verificationScoreUtil;

    private final VerificationProperties verificationProperties;

    public VerificationRuleEngine(
            List<VerificationRule> rules,
            VerificationScoreUtil verificationScoreUtil,
            VerificationProperties verificationProperties) {

        this.rules =
                sortRules(
                        rules
                );

        this.verificationScoreUtil =
                verificationScoreUtil;

        this.verificationProperties =
                verificationProperties;
    }

    public VerificationResult execute(
            VerificationResult verificationResult,
            VerificationContext context) {

        validateInput(
                verificationResult,
                context
        );

        long startedAt =
                System.nanoTime();

        verificationResult.setStatus(
                VerificationStatus.PROCESSING
        );

        verificationResult.setStartedAt(
                LocalDateTime.now()
        );

        verificationResult.setCompletedAt(
                null
        );

        verificationResult.setFailureReason(
                null
        );

        verificationResult.clearRuleResults();

        List<VerificationRuleResult> ruleResults =
                new ArrayList<>();

        for (VerificationRule rule : rules) {

            VerificationRuleResult ruleResult;

            try {

                if (!rule.supports(context)) {

                    ruleResult =
                            createSkippedResult(
                                    rule,
                                    "Rule is not applicable"
                            );

                } else {

                    ruleResult =
                            rule.evaluate(
                                    context
                            );
                }

                validateRuleResult(
                        rule,
                        ruleResult
                );

            } catch (Exception exception) {

                ruleResult =
                        createFailedRuleResult(
                                rule,
                                exception
                        );
            }

            ruleResults.add(
                    ruleResult
            );

            verificationResult.addRuleResult(
                    ruleResult
            );
        }

        updateRuleCounts(
                verificationResult,
                ruleResults
        );

        BigDecimal verificationScore =
                verificationScoreUtil.calculateScore(
                        ruleResults
                );

        verificationResult.setVerificationScore(
                verificationScore
        );

        VerificationStatus finalStatus =
                determineStatus(
                        verificationResult,
                        verificationScore
                );

        verificationResult.setStatus(
                finalStatus
        );

        verificationResult.setSummary(
                buildSummary(
                        verificationResult
                )
        );

        long processingDurationMs =
                Math.max(
                        (System.nanoTime() - startedAt)
                                / 1_000_000L,
                        0L
                );

        verificationResult.setProcessingDurationMs(
                processingDurationMs
        );

        verificationResult.setCompletedAt(
                LocalDateTime.now()
        );

        return verificationResult;
    }

    public List<VerificationRule> getRules() {

        return new ArrayList<>(
                rules
        );
    }

    public int getRuleCount() {
        return rules.size();
    }

    private VerificationStatus determineStatus(
            VerificationResult verificationResult,
            BigDecimal score) {

        if (verificationResult
                .getMandatoryFailureCount() > 0) {

            return VerificationStatus.REJECTED;
        }

        BigDecimal normalizedScore =
                score == null
                        ? BigDecimal.ZERO
                        : score;

        BigDecimal verifiedThreshold =
                BigDecimal.valueOf(
                                verificationProperties
                                        .getVerifiedScoreThreshold()
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal reviewThreshold =
                BigDecimal.valueOf(
                                verificationProperties
                                        .getReviewScoreThreshold()
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (normalizedScore.compareTo(
                verifiedThreshold) >= 0) {

            return VerificationStatus.VERIFIED;
        }

        if (normalizedScore.compareTo(
                reviewThreshold) >= 0) {

            return VerificationStatus.REVIEW_REQUIRED;
        }

        return VerificationStatus.REJECTED;
    }

    private void updateRuleCounts(
            VerificationResult verificationResult,
            List<VerificationRuleResult> ruleResults) {

        verificationResult.setTotalRuleCount(
                ruleResults.size()
        );

        verificationResult.setPassedRuleCount(
                toSafeInteger(
                        verificationScoreUtil.countOutcome(
                                ruleResults,
                                RuleOutcome.PASSED
                        )
                )
        );

        verificationResult.setFailedRuleCount(
                toSafeInteger(
                        verificationScoreUtil.countOutcome(
                                ruleResults,
                                RuleOutcome.FAILED
                        )
                )
        );

        verificationResult.setWarningRuleCount(
                toSafeInteger(
                        verificationScoreUtil.countOutcome(
                                ruleResults,
                                RuleOutcome.WARNING
                        )
                )
        );

        verificationResult.setSkippedRuleCount(
                toSafeInteger(
                        verificationScoreUtil.countOutcome(
                                ruleResults,
                                RuleOutcome.SKIPPED
                        )
                )
        );

        verificationResult.setMandatoryFailureCount(
                toSafeInteger(
                        verificationScoreUtil
                                .countMandatoryFailures(
                                        ruleResults
                                )
                )
        );
    }

    private String buildSummary(
            VerificationResult verificationResult) {

        String summary =
                "Verification completed with status "
                        + verificationResult.getStatus()
                        + ", score "
                        + formatScore(
                        verificationResult
                                .getVerificationScore()
                )
                        + ", total rules "
                        + verificationResult.getTotalRuleCount()
                        + ", passed rules "
                        + verificationResult.getPassedRuleCount()
                        + ", failed rules "
                        + verificationResult.getFailedRuleCount()
                        + ", warnings "
                        + verificationResult.getWarningRuleCount()
                        + ", skipped rules "
                        + verificationResult.getSkippedRuleCount()
                        + ", mandatory failures "
                        + verificationResult
                        .getMandatoryFailureCount();

        return limitText(
                summary,
                1000
        );
    }

    private VerificationRuleResult createSkippedResult(
            VerificationRule rule,
            String message) {

        VerificationRuleResult result =
                createBaseResult(
                        rule
                );

        result.setOutcome(
                RuleOutcome.SKIPPED
        );

        result.setScoreAwarded(
                BigDecimal.ZERO
        );

        result.setMessage(
                message
        );

        result.setActualValue(
                "Rule not applicable"
        );

        result.setExecutionDurationMs(
                0L
        );

        return result;
    }

    private VerificationRuleResult createFailedRuleResult(
            VerificationRule rule,
            Exception exception) {

        VerificationRuleResult result =
                createBaseResult(
                        rule
                );

        result.setOutcome(
                RuleOutcome.FAILED
        );

        result.setScoreAwarded(
                BigDecimal.ZERO
        );

        result.setMessage(
                limitText(
                        "Rule execution failed: "
                                + extractErrorMessage(
                                exception
                        ),
                        1000
                )
        );

        result.setActualValue(
                "Rule could not be evaluated"
        );

        result.setExecutionDurationMs(
                0L
        );

        return result;
    }

    private VerificationRuleResult createBaseResult(
            VerificationRule rule) {

        if (rule == null) {
            throw new IllegalArgumentException(
                    "Verification rule cannot be null"
            );
        }

        VerificationRuleResult result =
                new VerificationRuleResult();

        result.setRuleCode(
                normalizeRuleCode(
                        rule.getCode()
                )
        );

        result.setRuleName(
                normalizeRuleName(
                        rule.getName()
                )
        );

        result.setRuleDescription(
                limitText(
                        rule.getDescription(),
                        1000
                )
        );

        result.setCategory(
                rule.getCategory() == null
                        ? RuleCategory.GENERAL
                        : rule.getCategory()
        );

        result.setMandatory(
                rule.isMandatory()
        );

        result.setWeight(
                normalizeWeight(
                        rule.getWeight()
                )
        );

        result.setExecutionOrder(
                Math.max(
                        rule.getExecutionOrder(),
                        0
                )
        );

        return result;
    }

    private void validateRuleResult(
            VerificationRule rule,
            VerificationRuleResult result) {

        if (result == null) {
            throw new IllegalStateException(
                    "Rule returned no result: "
                            + normalizeRuleCode(
                            rule.getCode()
                    )
            );
        }

        if (result.getRuleCode() == null
                || result.getRuleCode().isBlank()) {

            result.setRuleCode(
                    normalizeRuleCode(
                            rule.getCode()
                    )
            );
        }

        if (result.getRuleName() == null
                || result.getRuleName().isBlank()) {

            result.setRuleName(
                    normalizeRuleName(
                            rule.getName()
                    )
            );
        }

        if (result.getRuleDescription() == null) {

            result.setRuleDescription(
                    limitText(
                            rule.getDescription(),
                            1000
                    )
            );
        }

        if (result.getCategory() == null) {

            result.setCategory(
                    rule.getCategory() == null
                            ? RuleCategory.GENERAL
                            : rule.getCategory()
            );
        }

        if (result.getOutcome() == null) {

            result.setOutcome(
                    RuleOutcome.SKIPPED
            );
        }

        if (result.getWeight() == null) {

            result.setWeight(
                    normalizeWeight(
                            rule.getWeight()
                    )
            );
        }

        if (result.getScoreAwarded() == null) {

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );
        }

        if (result.getScoreAwarded().compareTo(
                result.getWeight()) > 0) {

            result.setScoreAwarded(
                    result.getWeight()
            );
        }

        result.setMandatory(
                rule.isMandatory()
        );

        result.setExecutionOrder(
                Math.max(
                        rule.getExecutionOrder(),
                        0
                )
        );

        if (result.getExecutionDurationMs() == null) {

            result.setExecutionDurationMs(
                    0L
            );
        }
    }

    private void validateInput(
            VerificationResult verificationResult,
            VerificationContext context) {

        if (verificationResult == null) {

            throw new IllegalArgumentException(
                    "Verification result is required"
            );
        }

        if (context == null) {

            throw new IllegalArgumentException(
                    "Verification context is required"
            );
        }

        if (context.getDocumentId() == null
                || context.getDocumentId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid document ID is required"
            );
        }

        if (context.getOcrResultId() == null
                || context.getOcrResultId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid OCR result ID is required"
            );
        }

        if (verificationResult.getDocumentId() == null) {

            verificationResult.setDocumentId(
                    context.getDocumentId()
            );
        }

        if (verificationResult.getOcrResultId() == null) {

            verificationResult.setOcrResultId(
                    context.getOcrResultId()
            );
        }

        if (!verificationResult
                .getDocumentId()
                .equals(
                        context.getDocumentId()
                )) {

            throw new IllegalArgumentException(
                    "Verification document ID does not match "
                            + "the context document ID"
            );
        }

        if (!verificationResult
                .getOcrResultId()
                .equals(
                        context.getOcrResultId()
                )) {

            throw new IllegalArgumentException(
                    "Verification OCR result ID does not match "
                            + "the context OCR result ID"
            );
        }
    }

    private List<VerificationRule> sortRules(
            List<VerificationRule> sourceRules) {

        List<VerificationRule> sortedRules =
                sourceRules == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        sourceRules
                );

        sortedRules.removeIf(
                rule -> rule == null
        );

        sortedRules.sort(
                Comparator
                        .comparingInt(
                                VerificationRule
                                        ::getExecutionOrder
                        )
                        .thenComparing(
                                rule ->
                                        normalizeRuleCode(
                                                rule.getCode()
                                        )
                        )
        );

        return sortedRules;
    }

    private BigDecimal normalizeWeight(
            BigDecimal weight) {

        if (weight == null
                || weight.compareTo(
                BigDecimal.ZERO) < 0) {

            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return weight.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String normalizeRuleCode(
            String ruleCode) {

        if (ruleCode == null
                || ruleCode.isBlank()) {

            return "UNKNOWN_RULE";
        }

        return limitText(
                ruleCode.trim(),
                100
        );
    }

    private String normalizeRuleName(
            String ruleName) {

        if (ruleName == null
                || ruleName.isBlank()) {

            return "Unknown verification rule";
        }

        return limitText(
                ruleName.trim(),
                200
        );
    }

    private String extractErrorMessage(
            Exception exception) {

        if (exception == null) {
            return "Unknown rule execution error";
        }

        String errorMessage =
                exception.getMessage();

        if (errorMessage == null
                || errorMessage.isBlank()) {

            return exception.getClass()
                    .getSimpleName();
        }

        return errorMessage.trim();
    }

    private String formatScore(
            BigDecimal score) {

        if (score == null) {
            return "0.00";
        }

        return score.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private int toSafeInteger(
            long value) {

        if (value <= 0L) {
            return 0;
        }

        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }

    private String limitText(
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
}