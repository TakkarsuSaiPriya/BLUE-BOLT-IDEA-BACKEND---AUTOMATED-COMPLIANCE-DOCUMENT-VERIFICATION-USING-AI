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

class DocumentNumberRuleTest {

    private DocumentNumberRule documentNumberRule;

    @BeforeEach
    void setUp() {

        documentNumberRule =
                new DocumentNumberRule();
    }

    @Test
    @DisplayName(
            "Should detect a document number"
    )
    void shouldDetectDocumentNumber() {

        VerificationContext context =
                createContext(
                        "Document Number: REF-2026-1001"
                );

        VerificationRuleResult result =
                documentNumberRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "20.00"
                );

        assertThat(result.getActualValue())
                .startsWith(
                        "****"
                );

        assertThat(result.getActualValue())
                .doesNotContain(
                        "REF-2026-1001"
                );
    }

    @Test
    @DisplayName(
            "Should detect an application number"
    )
    void shouldDetectApplicationNumber() {

        VerificationContext context =
                createContext(
                        "Application No: APP-98765"
                );

        VerificationRuleResult result =
                documentNumberRule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );
    }

    @Test
    @DisplayName(
            "Should return warning when reference number is missing"
    )
    void shouldWarnWhenReferenceIsMissing() {

        VerificationContext context =
                createContext(
                        "Name: Sample User Status: Active"
                );

        VerificationRuleResult result =
                documentNumberRule.evaluate(
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
                .isEqualTo(
                        "Not found"
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