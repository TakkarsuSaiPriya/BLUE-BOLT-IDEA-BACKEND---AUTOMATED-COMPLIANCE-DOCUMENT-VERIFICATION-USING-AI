package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ConfidenceThresholdRule
        implements VerificationRule {

    private static final BigDecimal WEIGHT =
            new BigDecimal("20.00");

    private final VerificationProperties
            verificationProperties;

    public ConfidenceThresholdRule(
            VerificationProperties
                    verificationProperties) {

        this.verificationProperties =
                verificationProperties;
    }

    @Override
    public String getCode() {
        return "OCR_CONFIDENCE";
    }

    @Override
    public String getName() {
        return "OCR confidence threshold";
    }

    @Override
    public String getDescription() {

        return "Checks whether OCR confidence meets the configured minimum";
    }

    @Override
    public RuleCategory getCategory() {
        return RuleCategory.OCR_QUALITY;
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
        return 20;
    }

    @Override
    public VerificationRuleResult evaluate(
            VerificationContext context) {

        long startedAt =
                System.nanoTime();

        VerificationRuleResult result =
                createBaseResult();

        BigDecimal minimumConfidence =
                BigDecimal.valueOf(
                                verificationProperties
                                        .getMinimumOcrConfidence()
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal actualConfidence =
                context == null
                        ? null
                        : context.getOcrConfidence();

        result.setExpectedValue(
                "OCR confidence greater than or equal to "
                        + minimumConfidence
        );

        if (actualConfidence == null) {

            result.setOutcome(
                    RuleOutcome.WARNING
            );

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );

            result.setMessage(
                    "OCR confidence was not provided"
            );

            result.setActualValue(
                    "Not available"
            );

        } else if (actualConfidence.compareTo(
                minimumConfidence) >= 0) {

            result.setOutcome(
                    RuleOutcome.PASSED
            );

            result.setScoreAwarded(
                    WEIGHT
            );

            result.setMessage(
                    "OCR confidence meets the configured minimum"
            );

            result.setActualValue(
                    actualConfidence
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            .toString()
            );

        } else {

            result.setOutcome(
                    RuleOutcome.WARNING
            );

            BigDecimal partialScore =
                    calculatePartialScore(
                            actualConfidence,
                            minimumConfidence
                    );

            result.setScoreAwarded(
                    partialScore
            );

            result.setMessage(
                    "OCR confidence is below the configured minimum"
            );

            result.setActualValue(
                    actualConfidence
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            .toString()
            );
        }

        setDuration(
                result,
                startedAt
        );

        return result;
    }

    private BigDecimal calculatePartialScore(
            BigDecimal actual,
            BigDecimal minimum) {

        if (actual == null
                || actual.compareTo(
                BigDecimal.ZERO) <= 0
                || minimum.compareTo(
                BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal ratio =
                actual.divide(
                        minimum,
                        4,
                        RoundingMode.HALF_UP
                );

        if (ratio.compareTo(
                BigDecimal.ONE) > 0) {

            ratio = BigDecimal.ONE;
        }

        return WEIGHT.multiply(
                        ratio
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
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

        result.setExecutionDurationMs(
                Math.max(
                        (System.nanoTime() - startedAt)
                                / 1_000_000L,
                        0L
                )
        );
    }
}