package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SuspiciousPatternRule
        implements VerificationRule {

    private static final BigDecimal WEIGHT =
            new BigDecimal("10.00");

    private static final List<String> SUSPICIOUS_PATTERNS =
            List.of(
                    "lorem ipsum",
                    "undefined",
                    "null null",
                    "corrupted document",
                    "unable to read",
                    "no text detected"
            );

    @Override
    public String getCode() {
        return "SUSPICIOUS_PATTERN";
    }

    @Override
    public String getName() {
        return "Suspicious OCR pattern detection";
    }

    @Override
    public String getDescription() {

        return "Checks OCR text for known placeholder "
                + "or unreadable-content indicators";
    }

    @Override
    public RuleCategory getCategory() {
        return RuleCategory.SECURITY;
    }

    @Override
    public BigDecimal getWeight() {
        return WEIGHT;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public int getExecutionOrder() {
        return 50;
    }

    @Override
    public VerificationRuleResult evaluate(
            VerificationContext context) {

        long startedAt =
                System.nanoTime();

        VerificationRuleResult result =
                createBaseResult();

        String normalizedText =
                context == null
                        ? ""
                        : context.getNormalizedText();

        List<String> detectedPatterns =
                new ArrayList<>();

        for (String suspiciousPattern
                : SUSPICIOUS_PATTERNS) {

            if (normalizedText.contains(
                    suspiciousPattern)) {

                detectedPatterns.add(
                        suspiciousPattern
                );
            }
        }

        result.setExpectedValue(
                "No placeholder or unreadable-content patterns"
        );

        if (detectedPatterns.isEmpty()) {

            result.setOutcome(
                    RuleOutcome.PASSED
            );

            result.setScoreAwarded(
                    WEIGHT
            );

            result.setMessage(
                    "No suspicious OCR patterns were detected"
            );

            result.setActualValue(
                    "No suspicious patterns"
            );

        } else {

            result.setOutcome(
                    RuleOutcome.WARNING
            );

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );

            result.setMessage(
                    "Suspicious OCR content indicators were detected"
            );

            result.setActualValue(
                    String.join(
                            ", ",
                            detectedPatterns
                    )
            );
        }

        setDuration(
                result,
                startedAt
        );

        return result;
    }

    private VerificationRuleResult createBaseResult() {

        VerificationRuleResult result =
                new VerificationRuleResult();

        result.setRuleCode(
                getCode()
        );

        result.setRuleName(
                getName()
        );

        result.setRuleDescription(
                getDescription()
        );

        result.setCategory(
                getCategory()
        );

        result.setMandatory(
                isMandatory()
        );

        result.setWeight(
                getWeight()
        );

        result.setExecutionOrder(
                getExecutionOrder()
        );

        return result;
    }

    private void setDuration(
            VerificationRuleResult result,
            long startedAt) {

        long durationMs =
                Math.max(
                        (System.nanoTime() - startedAt)
                                / 1_000_000L,
                        0L
                );

        result.setExecutionDurationMs(
                durationMs
        );
    }
}