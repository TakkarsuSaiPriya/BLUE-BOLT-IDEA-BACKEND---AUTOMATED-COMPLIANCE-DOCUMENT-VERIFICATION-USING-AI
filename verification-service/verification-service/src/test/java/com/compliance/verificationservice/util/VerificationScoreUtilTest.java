package com.compliance.verificationservice.util;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationScoreUtilTest {

    private VerificationScoreUtil verificationScoreUtil;

    @BeforeEach
    void setUp() {

        verificationScoreUtil =
                new VerificationScoreUtil();
    }

    @Test
    @DisplayName(
            "Should return zero when rule results are null"
    )
    void shouldReturnZeroWhenResultsAreNull() {

        BigDecimal score =
                verificationScoreUtil.calculateScore(
                        null
                );

        assertThat(score)
                .isEqualByComparingTo(
                        "0.00"
                );
    }

    @Test
    @DisplayName(
            "Should return zero when rule results are empty"
    )
    void shouldReturnZeroWhenResultsAreEmpty() {

        BigDecimal score =
                verificationScoreUtil.calculateScore(
                        List.of()
                );

        assertThat(score)
                .isEqualByComparingTo(
                        "0.00"
                );
    }

    @Test
    @DisplayName(
            "Should calculate full score when all applicable rules pass"
    )
    void shouldCalculateFullScore() {

        VerificationRuleResult firstRule =
                createRuleResult(
                        RuleOutcome.PASSED,
                        "30.00",
                        "30.00",
                        true
                );

        VerificationRuleResult secondRule =
                createRuleResult(
                        RuleOutcome.PASSED,
                        "20.00",
                        "20.00",
                        false
                );

        VerificationRuleResult thirdRule =
                createRuleResult(
                        RuleOutcome.PASSED,
                        "50.00",
                        "50.00",
                        false
                );

        BigDecimal score =
                verificationScoreUtil.calculateScore(
                        List.of(
                                firstRule,
                                secondRule,
                                thirdRule
                        )
                );

        assertThat(score)
                .isEqualByComparingTo(
                        "100.00"
                );
    }

    @Test
    @DisplayName(
            "Should calculate weighted partial score"
    )
    void shouldCalculateWeightedPartialScore() {

        VerificationRuleResult firstRule =
                createRuleResult(
                        RuleOutcome.PASSED,
                        "30.00",
                        "30.00",
                        true
                );

        VerificationRuleResult secondRule =
                createRuleResult(
                        RuleOutcome.WARNING,
                        "20.00",
                        "10.00",
                        false
                );

        VerificationRuleResult thirdRule =
                createRuleResult(
                        RuleOutcome.FAILED,
                        "50.00",
                        "0.00",
                        false
                );

        BigDecimal score =
                verificationScoreUtil.calculateScore(
                        List.of(
                                firstRule,
                                secondRule,
                                thirdRule
                        )
                );

        assertThat(score)
                .isEqualByComparingTo(
                        "40.00"
                );
    }

    @Test
    @DisplayName(
            "Should exclude skipped rules from total weight"
    )
    void shouldExcludeSkippedRulesFromTotalWeight() {

        VerificationRuleResult passedRule =
                createRuleResult(
                        RuleOutcome.PASSED,
                        "50.00",
                        "50.00",
                        false
                );

        VerificationRuleResult skippedRule =
                createRuleResult(
                        RuleOutcome.SKIPPED,
                        "50.00",
                        "0.00",
                        false
                );

        BigDecimal score =
                verificationScoreUtil.calculateScore(
                        List.of(
                                passedRule,
                                skippedRule
                        )
                );

        assertThat(score)
                .isEqualByComparingTo(
                        "100.00"
                );
    }

    @Test
    @DisplayName(
            "Should count rule outcomes"
    )
    void shouldCountOutcomes() {

        List<VerificationRuleResult> results =
                List.of(
                        createRuleResult(
                                RuleOutcome.PASSED,
                                "10.00",
                                "10.00",
                                false
                        ),
                        createRuleResult(
                                RuleOutcome.PASSED,
                                "10.00",
                                "10.00",
                                false
                        ),
                        createRuleResult(
                                RuleOutcome.WARNING,
                                "10.00",
                                "5.00",
                                false
                        ),
                        createRuleResult(
                                RuleOutcome.FAILED,
                                "10.00",
                                "0.00",
                                true
                        )
                );

        assertThat(
                verificationScoreUtil.countOutcome(
                        results,
                        RuleOutcome.PASSED
                )
        ).isEqualTo(2L);

        assertThat(
                verificationScoreUtil.countOutcome(
                        results,
                        RuleOutcome.WARNING
                )
        ).isEqualTo(1L);

        assertThat(
                verificationScoreUtil.countOutcome(
                        results,
                        RuleOutcome.FAILED
                )
        ).isEqualTo(1L);
    }

    @Test
    @DisplayName(
            "Should count only mandatory failed rules"
    )
    void shouldCountMandatoryFailures() {

        List<VerificationRuleResult> results =
                List.of(
                        createRuleResult(
                                RuleOutcome.FAILED,
                                "20.00",
                                "0.00",
                                true
                        ),
                        createRuleResult(
                                RuleOutcome.FAILED,
                                "20.00",
                                "0.00",
                                false
                        ),
                        createRuleResult(
                                RuleOutcome.PASSED,
                                "20.00",
                                "20.00",
                                true
                        )
                );

        long mandatoryFailures =
                verificationScoreUtil
                        .countMandatoryFailures(
                                results
                        );

        assertThat(mandatoryFailures)
                .isEqualTo(1L);
    }

    private VerificationRuleResult createRuleResult(
            RuleOutcome outcome,
            String weight,
            String scoreAwarded,
            boolean mandatory) {

        VerificationRuleResult result =
                new VerificationRuleResult();

        result.setOutcome(
                outcome
        );

        result.setWeight(
                new BigDecimal(
                        weight
                )
        );

        result.setScoreAwarded(
                new BigDecimal(
                        scoreAwarded
                )
        );

        result.setMandatory(
                mandatory
        );

        return result;
    }
}