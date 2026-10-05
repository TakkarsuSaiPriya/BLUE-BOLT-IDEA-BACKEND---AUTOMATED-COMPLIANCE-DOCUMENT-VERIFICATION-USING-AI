package com.compliance.verificationservice.util;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleOutcome;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class VerificationScoreUtil {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100.00");

    public BigDecimal calculateScore(
            List<VerificationRuleResult> ruleResults) {

        if (ruleResults == null
                || ruleResults.isEmpty()) {

            return ZERO;
        }

        BigDecimal totalWeight =
                ZERO;

        BigDecimal awardedScore =
                ZERO;

        for (VerificationRuleResult result
                : ruleResults) {

            if (result == null
                    || result.getOutcome()
                    == RuleOutcome.SKIPPED) {

                continue;
            }

            BigDecimal weight =
                    normalizeNonNegative(
                            result.getWeight()
                    );

            BigDecimal score =
                    normalizeNonNegative(
                            result.getScoreAwarded()
                    );

            if (score.compareTo(weight) > 0) {
                score = weight;
            }

            totalWeight =
                    totalWeight.add(
                            weight
                    );

            awardedScore =
                    awardedScore.add(
                            score
                    );
        }

        if (totalWeight.compareTo(
                BigDecimal.ZERO) <= 0) {

            return ZERO;
        }

        BigDecimal percentage =
                awardedScore
                        .multiply(
                                ONE_HUNDRED
                        )
                        .divide(
                                totalWeight,
                                2,
                                RoundingMode.HALF_UP
                        );

        return clampPercentage(
                percentage
        );
    }

    public long countOutcome(
            List<VerificationRuleResult> results,
            RuleOutcome outcome) {

        if (results == null
                || results.isEmpty()
                || outcome == null) {

            return 0L;
        }

        return results.stream()
                .filter(result ->
                        result != null
                                && outcome
                                == result.getOutcome()
                )
                .count();
    }

    public long countMandatoryFailures(
            List<VerificationRuleResult> results) {

        if (results == null
                || results.isEmpty()) {

            return 0L;
        }

        return results.stream()
                .filter(result ->
                        result != null
                                && result.isMandatory()
                                && result.getOutcome()
                                == RuleOutcome.FAILED
                )
                .count();
    }

    private BigDecimal normalizeNonNegative(
            BigDecimal value) {

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return ZERO;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal clampPercentage(
            BigDecimal value) {

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return ZERO;
        }

        if (value.compareTo(
                ONE_HUNDRED) > 0) {

            return ONE_HUNDRED;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}