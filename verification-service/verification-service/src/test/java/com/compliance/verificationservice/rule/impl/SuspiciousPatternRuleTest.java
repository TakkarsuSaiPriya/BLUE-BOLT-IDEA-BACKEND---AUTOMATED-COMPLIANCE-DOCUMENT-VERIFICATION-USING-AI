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

class SuspiciousPatternRuleTest {

    private SuspiciousPatternRule
            suspiciousPatternRule;

    @BeforeEach
    void setUp() {

        suspiciousPatternRule =
                new SuspiciousPatternRule();
    }

    @Test
    @DisplayName(
            "Should pass when no suspicious pattern exists"
    )
    void shouldPassWithoutSuspiciousPatterns() {

        VerificationContext context =
                createContext(
                        "Document Number: REF-1001 "
                                + "Name: Sample User Status: Active"
                );

        VerificationRuleResult result =
                suspiciousPatternRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "10.00"
                );
    }

    @Test
    @DisplayName(
            "Should warn when lorem ipsum is detected"
    )
    void shouldWarnForLoremIpsum() {

        VerificationContext context =
                createContext(
                        "Lorem Ipsum placeholder document"
                );

        VerificationRuleResult result =
                suspiciousPatternRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.WARNING
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "0.00"
                );

        assertThat(result.getActualValue())
                .contains(
                        "lorem ipsum"
                );
    }

    @Test
    @DisplayName(
            "Should report all detected suspicious patterns"
    )
    void shouldReportMultiplePatterns() {

        VerificationContext context =
                createContext(
                        "The document contains undefined "
                                + "and no text detected"
                );

        VerificationRuleResult result =
                suspiciousPatternRule.evaluate(
                        context
                );

        assertThat(result.getActualValue())
                .contains(
                        "undefined"
                )
                .contains(
                        "no text detected"
                );
    }

    private VerificationContext createContext(
            String extractedText) {

        return new VerificationContext(
                1L,
                2L,
                "document.png",
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