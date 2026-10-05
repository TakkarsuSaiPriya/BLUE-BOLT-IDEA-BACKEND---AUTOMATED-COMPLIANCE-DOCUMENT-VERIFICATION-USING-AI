package com.compliance.verificationservice.rule.impl;

import com.compliance.verificationservice.config.VerificationProperties;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.rule.VerificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfidenceThresholdRuleTest {

    private VerificationProperties verificationProperties;

    private ConfidenceThresholdRule rule;

    @BeforeEach
    void setUp() {

        verificationProperties =
                mock(VerificationProperties.class);

        when(
                verificationProperties
                        .getMinimumOcrConfidence()
        ).thenReturn(40.0);

        rule =
                new ConfidenceThresholdRule(
                        verificationProperties
                );
    }

    @Test
    @DisplayName(
            "Should pass when OCR confidence equals the minimum"
    )
    void shouldPassAtMinimumConfidence() {

        VerificationContext context =
                createContext(
                        new BigDecimal("40.00")
                );

        VerificationRuleResult result =
                rule.evaluate(
                        context
                );

        assertThat(result.getRuleCode())
                .isEqualTo(
                        "OCR_CONFIDENCE"
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "20.00"
                );
    }

    @Test
    @DisplayName(
            "Should pass when OCR confidence is above the minimum"
    )
    void shouldPassAboveMinimumConfidence() {

        VerificationContext context =
                createContext(
                        new BigDecimal("92.50")
                );

        VerificationRuleResult result =
                rule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(result.getActualValue())
                .isEqualTo(
                        "92.50"
                );
    }

    @Test
    @DisplayName(
            "Should return partial score below the minimum"
    )
    void shouldReturnPartialScoreBelowMinimum() {

        VerificationContext context =
                createContext(
                        new BigDecimal("20.00")
                );

        VerificationRuleResult result =
                rule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.WARNING
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "10.00"
                );
    }

    @Test
    @DisplayName(
            "Should return warning when OCR confidence is unavailable"
    )
    void shouldWarnWhenConfidenceIsUnavailable() {

        VerificationContext context =
                createContext(
                        null
                );

        VerificationRuleResult result =
                rule.evaluate(
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
                        "Not available"
                );
    }

    @Test
    @DisplayName(
            "Should use the configured confidence threshold"
    )
    void shouldUseConfiguredThreshold() {

        when(
                verificationProperties
                        .getMinimumOcrConfidence()
        ).thenReturn(80.0);

        VerificationContext context =
                createContext(
                        new BigDecimal("60.00")
                );

        VerificationRuleResult result =
                rule.evaluate(
                        context
                );

        assertThat(result.getOutcome())
                .isEqualTo(
                        RuleOutcome.WARNING
                );

        assertThat(result.getScoreAwarded())
                .isEqualByComparingTo(
                        "15.00"
                );

        assertThat(result.getExpectedValue())
                .contains(
                        "80.00"
                );
    }

    private VerificationContext createContext(
            BigDecimal confidence) {

        String text =
                "Document Number: REF-1001 "
                        + "Name: Sample User Status: Active";

        return new VerificationContext(
                1L,
                2L,
                "sample.png",
                "GENERAL",
                text,
                confidence,
                1,
                text.length(),
                8,
                "testuser",
                Map.of()
        );
    }
}