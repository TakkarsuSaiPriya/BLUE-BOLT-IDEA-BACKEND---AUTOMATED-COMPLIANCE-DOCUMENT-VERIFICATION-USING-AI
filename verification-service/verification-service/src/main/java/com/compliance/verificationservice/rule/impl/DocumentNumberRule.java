package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentNumberRule implements VerificationRule {

    private static final BigDecimal WEIGHT =
            new BigDecimal("20.00");

    private static final Pattern DOCUMENT_NUMBER_PATTERN =
            Pattern.compile(
                    "(?i)"
                            + "(document|application|reference)"
                            + "\\s*"
                            + "(number|no\\.?|code)?"
                            + "\\s*"
                            + "[:#-]?"
                            + "\\s*"
                            + "([a-z0-9][a-z0-9\\-/]{3,49})"
            );

    @Override
    public String getCode() {
        return "DOCUMENT_NUMBER";
    }

    @Override
    public String getName() {
        return "Document number detection";
    }

    @Override
    public String getDescription() {
        return "Checks whether OCR text contains a document, "
                + "application, or reference number";
    }

    @Override
    public RuleCategory getCategory() {
        return RuleCategory.DOCUMENT_NUMBER;
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
        return 40;
    }

    @Override
    public VerificationRuleResult evaluate(
            VerificationContext context) {

        long startedAt =
                System.nanoTime();

        VerificationRuleResult result =
                createBaseResult();

        String extractedText =
                context == null
                        ? ""
                        : context.getExtractedText();

        Matcher matcher =
                DOCUMENT_NUMBER_PATTERN.matcher(
                        extractedText == null
                                ? ""
                                : extractedText
                );

        result.setExpectedValue(
                "Document, application, or reference number"
        );

        if (matcher.find()) {

            String detectedNumber =
                    matcher.group(3);

            result.setOutcome(
                    RuleOutcome.PASSED
            );

            result.setScoreAwarded(
                    WEIGHT
            );

            result.setMessage(
                    "Document reference number was detected"
            );

            result.setActualValue(
                    maskReference(
                            detectedNumber
                    )
            );

        } else {

            result.setOutcome(
                    RuleOutcome.WARNING
            );

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );

            result.setMessage(
                    "No document reference number was detected"
            );

            result.setActualValue(
                    "Not found"
            );
        }

        setDuration(
                result,
                startedAt
        );

        return result;
    }

    private String maskReference(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "Detected";
        }

        String normalizedValue =
                value.trim();

        if (normalizedValue.length() <= 4) {
            return "****";
        }

        return "****"
                + normalizedValue.substring(
                normalizedValue.length() - 4
        );
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