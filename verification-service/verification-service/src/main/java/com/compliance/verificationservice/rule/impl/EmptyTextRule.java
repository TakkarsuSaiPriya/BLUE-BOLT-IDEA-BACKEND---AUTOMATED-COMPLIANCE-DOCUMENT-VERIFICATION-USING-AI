package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmptyTextRule
        implements VerificationRule {

    private static final BigDecimal WEIGHT =
            new BigDecimal("30.00");

    private static final int MINIMUM_TEXT_LENGTH =
            10;

    @Override
    public String getCode() {
        return "EMPTY_TEXT";
    }

    @Override
    public String getName() {
        return "OCR text availability";
    }

    @Override
    public String getDescription() {

        return "Checks whether OCR produced sufficient readable text";
    }

    @Override
    public RuleCategory getCategory() {
        return RuleCategory.CONTENT;
    }

    @Override
    public BigDecimal getWeight() {
        return WEIGHT;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public int getExecutionOrder() {
        return 10;
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

        if (normalizedText.length()
                >= MINIMUM_TEXT_LENGTH) {

            result.setOutcome(
                    RuleOutcome.PASSED
            );

            result.setScoreAwarded(
                    WEIGHT
            );

            result.setMessage(
                    "OCR text is available for verification"
            );

            result.setExpectedValue(
                    "At least "
                            + MINIMUM_TEXT_LENGTH
                            + " readable characters"
            );

            result.setActualValue(
                    normalizedText.length()
                            + " readable characters"
            );

        } else {

            result.setOutcome(
                    RuleOutcome.FAILED
            );

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );

            result.setMessage(
                    "OCR text is empty or too short"
            );

            result.setExpectedValue(
                    "At least "
                            + MINIMUM_TEXT_LENGTH
                            + " readable characters"
            );

            result.setActualValue(
                    normalizedText.length()
                            + " readable characters"
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

        long duration =
                Math.max(
                        (System.nanoTime() - startedAt)
                                / 1_000_000L,
                        0L
                );

        result.setExecutionDurationMs(
                duration
        );
    }
}