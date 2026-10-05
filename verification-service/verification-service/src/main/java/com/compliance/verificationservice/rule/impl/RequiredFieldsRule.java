package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import com.compliance.verificationservice.rule.VerificationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class RequiredFieldsRule implements VerificationRule {

    private static final BigDecimal WEIGHT =
            new BigDecimal("20.00");

    private static final List<String> REQUIRED_FIELDS =
            List.of("name", "status");

    @Override
    public String getCode() {
        return "REQUIRED_FIELDS";
    }

    @Override
    public String getName() {
        return "Required document fields";
    }

    @Override
    public String getDescription() {
        return "Checks whether required field labels exist "
                + "in the extracted OCR text";
    }

    @Override
    public RuleCategory getCategory() {
        return RuleCategory.REQUIRED_FIELD;
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
        return 30;
    }

    @Override
    public VerificationRuleResult evaluate(
            VerificationContext context) {

        long startedAt = System.nanoTime();

        VerificationRuleResult result =
                createBaseResult();

        String text =
                context == null
                        ? ""
                        : context.getNormalizedText();

        List<String> foundFields =
                new ArrayList<>();

        List<String> missingFields =
                new ArrayList<>();

        for (String requiredField : REQUIRED_FIELDS) {

            if (containsFieldLabel(
                    text,
                    requiredField
            )) {

                foundFields.add(requiredField);

            } else {

                missingFields.add(requiredField);
            }
        }

        int totalFieldCount =
                REQUIRED_FIELDS.size();

        int foundFieldCount =
                foundFields.size();

        result.setExpectedValue(
                String.join(
                        ", ",
                        REQUIRED_FIELDS
                )
        );

        result.setActualValue(
                foundFields.isEmpty()
                        ? "No required field labels found"
                        : String.join(
                        ", ",
                        foundFields
                )
        );

        if (foundFieldCount == totalFieldCount) {

            result.setOutcome(
                    RuleOutcome.PASSED
            );

            result.setScoreAwarded(
                    WEIGHT
            );

            result.setMessage(
                    "All required field labels were detected"
            );

        } else if (foundFieldCount > 0) {

            result.setOutcome(
                    RuleOutcome.WARNING
            );

            result.setScoreAwarded(
                    calculateProportionalScore(
                            foundFieldCount,
                            totalFieldCount
                    )
            );

            result.setMessage(
                    "Missing required field labels: "
                            + String.join(
                            ", ",
                            missingFields
                    )
            );

        } else {

            result.setOutcome(
                    RuleOutcome.FAILED
            );

            result.setScoreAwarded(
                    BigDecimal.ZERO
            );

            result.setMessage(
                    "Required field labels were not detected"
            );
        }

        long durationMs =
                Math.max(
                        (System.nanoTime() - startedAt)
                                / 1_000_000L,
                        0L
                );

        result.setExecutionDurationMs(
                durationMs
        );

        return result;
    }

    private boolean containsFieldLabel(
            String text,
            String label) {

        if (text == null
                || text.isBlank()
                || label == null
                || label.isBlank()) {

            return false;
        }

        return text.contains(label + ":")
                || text.contains(label + " :")
                || text.contains(label + " ");
    }

    private BigDecimal calculateProportionalScore(
            int foundFieldCount,
            int totalFieldCount) {

        if (foundFieldCount <= 0
                || totalFieldCount <= 0) {

            return new BigDecimal("0.00");
        }

        return WEIGHT
                .multiply(
                        BigDecimal.valueOf(
                                foundFieldCount
                        )
                )
                .divide(
                        BigDecimal.valueOf(
                                totalFieldCount
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private VerificationRuleResult createBaseResult() {

        VerificationRuleResult result =
                new VerificationRuleResult();

        result.setRuleCode(getCode());
        result.setRuleName(getName());
        result.setRuleDescription(getDescription());
        result.setCategory(getCategory());
        result.setMandatory(isMandatory());
        result.setWeight(getWeight());
        result.setExecutionOrder(getExecutionOrder());

        return result;
    }
}