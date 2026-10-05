package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RequiredFieldsRuleTest {

    private RequiredFieldsRule rule;

    @BeforeEach
    void setUp() {
        rule = new RequiredFieldsRule();
    }

    @Test
    @DisplayName("Should pass when all required fields are present")
    void shouldPassWhenAllFieldsArePresent() {

        VerificationContext context =
                createContext(
                        "Name: Sample Applicant Status: Active"
                );

        VerificationRuleResult result =
                rule.evaluate(context);

        assertThat(result.getRuleCode())
                .isEqualTo("REQUIRED_FIELDS");

        assertThat(result.getOutcome())
                .isEqualTo(RuleOutcome.PASSED);

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Should warn when one required field is present")
    void shouldWarnWhenOneFieldIsPresent() {

        VerificationContext context =
                createContext(
                        "Name: Sample Applicant"
                );

        VerificationRuleResult result =
                rule.evaluate(context);

        assertThat(result.getOutcome())
                .isEqualTo(RuleOutcome.WARNING);

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo("10.00");

        assertThat(result.getMessage())
                .contains("status");
    }

    @Test
    @DisplayName("Should fail when required fields are absent")
    void shouldFailWhenNoFieldsArePresent() {

        VerificationContext context =
                createContext(
                        "Document Number: REF-1001"
                );

        VerificationRuleResult result =
                rule.evaluate(context);

        assertThat(result.getOutcome())
                .isEqualTo(RuleOutcome.FAILED);

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should detect uppercase field labels")
    void shouldDetectUppercaseFieldLabels() {

        VerificationContext context =
                createContext(
                        "NAME : SAMPLE APPLICANT STATUS : ACTIVE"
                );

        VerificationRuleResult result =
                rule.evaluate(context);

        assertThat(result.getOutcome())
                .isEqualTo(RuleOutcome.PASSED);
    }

    private VerificationContext createContext(
            String extractedText) {

        return new VerificationContext(
                1L,
                2L,
                "sample.png",
                "GENERAL",
                extractedText,
                new BigDecimal("90.00"),
                1,
                extractedText.length(),
                5,
                "testuser",
                Map.of()
        );
    }
}