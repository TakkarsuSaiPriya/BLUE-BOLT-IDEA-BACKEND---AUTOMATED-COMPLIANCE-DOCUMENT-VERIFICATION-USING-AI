package com.compliance.verificationservice.rule;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.util.VerificationScoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificationRuleEngineTest {

    private VerificationProperties verificationProperties;

    private VerificationRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {

        verificationProperties =
                mock(VerificationProperties.class);

        when(
                verificationProperties
                        .getVerifiedScoreThreshold()
        ).thenReturn(80.0);

        when(
                verificationProperties
                        .getReviewScoreThreshold()
        ).thenReturn(50.0);
    }

    @Test
    @DisplayName(
            "Should verify when score reaches verified threshold"
    )
    void shouldReturnVerifiedStatus() {

        VerificationRule firstRule =
                createRule(
                        "RULE_ONE",
                        20,
                        false,
                        RuleOutcome.PASSED,
                        "60.00",
                        "60.00"
                );

        VerificationRule secondRule =
                createRule(
                        "RULE_TWO",
                        10,
                        false,
                        RuleOutcome.PASSED,
                        "40.00",
                        "40.00"
                );

        createEngine(
                List.of(
                        firstRule,
                        secondRule
                )
        );

        VerificationResult result =
                ruleEngine.execute(
                        createVerificationResult(),
                        createContext()
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.VERIFIED
                );

        assertThat(result.getVerificationScore())
                .isEqualByComparingTo(
                        "100.00"
                );

        assertThat(result.getTotalRuleCount())
                .isEqualTo(2);

        assertThat(result.getPassedRuleCount())
                .isEqualTo(2);

        assertThat(result.getRuleResults())
                .hasSize(2);
    }

    @Test
    @DisplayName(
            "Should require review for middle-range score"
    )
    void shouldReturnReviewRequiredStatus() {

        VerificationRule passedRule =
                createRule(
                        "PASSED_RULE",
                        10,
                        false,
                        RuleOutcome.PASSED,
                        "60.00",
                        "60.00"
                );

        VerificationRule failedRule =
                createRule(
                        "FAILED_RULE",
                        20,
                        false,
                        RuleOutcome.FAILED,
                        "40.00",
                        "0.00"
                );

        createEngine(
                List.of(
                        passedRule,
                        failedRule
                )
        );

        VerificationResult result =
                ruleEngine.execute(
                        createVerificationResult(),
                        createContext()
                );

        assertThat(result.getVerificationScore())
                .isEqualByComparingTo(
                        "60.00"
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.REVIEW_REQUIRED
                );

        assertThat(result.getFailedRuleCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName(
            "Should reject when mandatory rule fails"
    )
    void shouldRejectWhenMandatoryRuleFails() {

        VerificationRule mandatoryFailure =
                createRule(
                        "MANDATORY_FAILURE",
                        10,
                        true,
                        RuleOutcome.FAILED,
                        "10.00",
                        "0.00"
                );

        VerificationRule highScoreRule =
                createRule(
                        "HIGH_SCORE",
                        20,
                        false,
                        RuleOutcome.PASSED,
                        "90.00",
                        "90.00"
                );

        createEngine(
                List.of(
                        mandatoryFailure,
                        highScoreRule
                )
        );

        VerificationResult result =
                ruleEngine.execute(
                        createVerificationResult(),
                        createContext()
                );

        assertThat(result.getVerificationScore())
                .isEqualByComparingTo(
                        "90.00"
                );

        assertThat(result.getMandatoryFailureCount())
                .isEqualTo(1);

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.REJECTED
                );
    }

    @Test
    @DisplayName(
            "Should execute rules by execution order"
    )
    void shouldSortRulesByExecutionOrder() {

        VerificationRule laterRule =
                createRule(
                        "LATER",
                        50,
                        false,
                        RuleOutcome.PASSED,
                        "50.00",
                        "50.00"
                );

        VerificationRule earlierRule =
                createRule(
                        "EARLIER",
                        10,
                        false,
                        RuleOutcome.PASSED,
                        "50.00",
                        "50.00"
                );

        createEngine(
                List.of(
                        laterRule,
                        earlierRule
                )
        );

        VerificationResult result =
                ruleEngine.execute(
                        createVerificationResult(),
                        createContext()
                );

        assertThat(result.getRuleResults())
                .extracting(
                        VerificationRuleResult::getRuleCode
                )
                .containsExactly(
                        "EARLIER",
                        "LATER"
                );
    }

    @Test
    @DisplayName(
            "Should convert a rule exception into failed rule result"
    )
    void shouldHandleRuleException() {

        VerificationRule failingRule =
                new VerificationRule() {

                    @Override
                    public String getCode() {
                        return "EXCEPTION_RULE";
                    }

                    @Override
                    public String getName() {
                        return "Exception rule";
                    }

                    @Override
                    public String getDescription() {
                        return "Throws an exception";
                    }

                    @Override
                    public RuleCategory getCategory() {
                        return RuleCategory.GENERAL;
                    }

                    @Override
                    public BigDecimal getWeight() {
                        return new BigDecimal("100.00");
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

                        throw new IllegalStateException(
                                "Rule failure"
                        );
                    }
                };

        createEngine(
                List.of(
                        failingRule
                )
        );

        VerificationResult result =
                ruleEngine.execute(
                        createVerificationResult(),
                        createContext()
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.REJECTED
                );

        assertThat(result.getFailedRuleCount())
                .isEqualTo(1);

        assertThat(result.getMandatoryFailureCount())
                .isEqualTo(1);

        assertThat(result.getRuleResults().getFirst()
                .getMessage())
                .contains(
                        "Rule failure"
                );
    }

    @Test
    @DisplayName(
            "Should reject null verification result"
    )
    void shouldRejectNullVerificationResult() {

        createEngine(
                List.of()
        );

        assertThatThrownBy(() ->
                ruleEngine.execute(
                        null,
                        createContext()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Verification result"
                );
    }

    private void createEngine(
            List<VerificationRule> rules) {

        ruleEngine =
                new VerificationRuleEngine(
                        rules,
                        new VerificationScoreUtil(),
                        verificationProperties
                );
    }

    private VerificationRule createRule(
            String code,
            int executionOrder,
            boolean mandatory,
            RuleOutcome outcome,
            String weight,
            String awardedScore) {

        return new VerificationRule() {

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getName() {
                return code + " name";
            }

            @Override
            public String getDescription() {
                return code + " description";
            }

            @Override
            public RuleCategory getCategory() {
                return RuleCategory.GENERAL;
            }

            @Override
            public BigDecimal getWeight() {
                return new BigDecimal(
                        weight
                );
            }

            @Override
            public boolean isMandatory() {
                return mandatory;
            }

            @Override
            public int getExecutionOrder() {
                return executionOrder;
            }

            @Override
            public VerificationRuleResult evaluate(
                    VerificationContext context) {

                VerificationRuleResult result =
                        new VerificationRuleResult();

                result.setRuleCode(
                        code
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

                result.setOutcome(
                        outcome
                );

                result.setMandatory(
                        mandatory
                );

                result.setWeight(
                        new BigDecimal(
                                weight
                        )
                );

                result.setScoreAwarded(
                        new BigDecimal(
                                awardedScore
                        )
                );

                result.setExecutionOrder(
                        executionOrder
                );

                result.setExecutionDurationMs(
                        1L
                );

                return result;
            }
        };
    }

    private VerificationResult createVerificationResult() {

        VerificationResult result =
                new VerificationResult();

        result.setDocumentId(
                1L
        );

        result.setOcrResultId(
                2L
        );

        result.setRequestedBy(
                "testuser"
        );

        result.setStatus(
                VerificationStatus.PENDING
        );

        return result;
    }

    private VerificationContext createContext() {

        String text =
                "Document Number: REF-1001 "
                        + "Name: Sample User Status: Active";

        return new VerificationContext(
                1L,
                2L,
                "sample.png",
                "GENERAL",
                text,
                new BigDecimal("90.00"),
                1,
                text.length(),
                8,
                "testuser",
                Map.of()
        );
    }
}