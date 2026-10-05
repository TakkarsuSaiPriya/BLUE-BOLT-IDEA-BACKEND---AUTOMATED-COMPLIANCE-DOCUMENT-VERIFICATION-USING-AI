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

class EmptyTextRuleTest {

    private EmptyTextRule emptyTextRule;

    @BeforeEach
    void setUp() {

        emptyTextRule =
                new EmptyTextRule();
    }

    @Test
    @DisplayName(
            "Should pass when OCR text has sufficient content"
    )
    void shouldPassForSufficientText() {

        VerificationContext context =
                createContext(
                        "Document Number: REF-1001 "
                                + "Name: Sample Applicant"
                );

        VerificationRuleResult result =
                emptyTextRule.evaluate(
                        context
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getRuleCode())
                .isEqualTo(
                        "EMPTY_TEXT"
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "30.00"
                );

        assertThat(result.isMandatory())
                .isTrue();
    }

    @Test
    @DisplayName(
            "Should fail when OCR text is empty"
    )
    void shouldFailForEmptyText() {

        VerificationContext context =
                createContext(
                        ""
                );

        VerificationRuleResult result =
                emptyTextRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.FAILED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        BigDecimal.ZERO
                );

        assertThat(result.isMandatory())
                .isTrue();
    }

    @Test
    @DisplayName(
            "Should fail when OCR text is shorter than minimum length"
    )
    void shouldFailForShortText() {

        VerificationContext context =
                createContext(
                        "Short"
                );

        VerificationRuleResult result =
                emptyTextRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.FAILED
                );

        assertThat(result.getActualValue())
                .contains(
                        "5 readable characters"
                );
    }

    @Test
    @DisplayName(
            "Should fail safely when verification context is null"
    )
    void shouldFailWhenContextIsNull() {

        VerificationRuleResult result =
                emptyTextRule.evaluate(
                        null
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.FAILED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "0.00"
                );
    }

    private VerificationContext createContext(
            String extractedText) {

        return new VerificationContext(
                1L,
                2L,
                "sample.png",
                "GENERAL",
                extractedText,
                new BigDecimal("88.50"),
                1,
                extractedText == null
                        ? 0
                        : extractedText.length(),
                5,
                "testuser",
                Map.of()
        );
    }
}