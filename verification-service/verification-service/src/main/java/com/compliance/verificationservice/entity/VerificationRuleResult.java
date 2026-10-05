package com.compliance.verificationservice.entity;

import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "verification_rule_results",
        indexes = {
                @Index(
                        name = "idx_rule_verification_result_id",
                        columnList = "verification_result_id"
                ),
                @Index(
                        name = "idx_rule_name",
                        columnList = "rule_name"
                ),
                @Index(
                        name = "idx_rule_outcome",
                        columnList = "outcome"
                ),
                @Index(
                        name = "idx_rule_category",
                        columnList = "category"
                )
        }
)
public class VerificationRuleResult extends BaseEntity {

    private static final BigDecimal ZERO_DECIMAL =
            new BigDecimal("0.00");

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "verification_result_id",
            nullable = false
    )
    private VerificationResult verificationResult;

    @Column(
            name = "rule_code",
            nullable = false,
            length = 100
    )
    private String ruleCode;

    @Column(
            name = "rule_name",
            nullable = false,
            length = 200
    )
    private String ruleName;

    @Column(
            name = "rule_description",
            length = 1000
    )
    private String ruleDescription;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private RuleCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "outcome",
            nullable = false,
            length = 30
    )
    private RuleOutcome outcome;

    @Column(
            name = "mandatory",
            nullable = false
    )
    private boolean mandatory;

    @Column(
            name = "weight",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal weight =
            ZERO_DECIMAL;

    @Column(
            name = "score_awarded",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal scoreAwarded =
            ZERO_DECIMAL;

    @Column(
            name = "message",
            length = 1000
    )
    private String message;

    @Column(
            name = "expected_value",
            length = 1000
    )
    private String expectedValue;

    @Column(
            name = "actual_value",
            length = 1000
    )
    private String actualValue;

    @Column(
            name = "execution_order",
            nullable = false
    )
    private int executionOrder;

    @Column(
            name = "execution_duration_ms"
    )
    private Long executionDurationMs;

    @PrePersist
    protected void initializeRuleResult() {

        if (category == null) {
            category =
                    RuleCategory.GENERAL;
        }

        if (outcome == null) {
            outcome =
                    RuleOutcome.SKIPPED;
        }

        ruleCode =
                normalizeRequiredText(
                        ruleCode,
                        "Rule code",
                        100
                );

        ruleName =
                normalizeRequiredText(
                        ruleName,
                        "Rule name",
                        200
                );

        ruleDescription =
                normalizeNullableText(
                        ruleDescription,
                        1000
                );

        message =
                normalizeNullableText(
                        message,
                        1000
                );

        expectedValue =
                normalizeNullableText(
                        expectedValue,
                        1000
                );

        actualValue =
                normalizeNullableText(
                        actualValue,
                        1000
                );

        weight =
                normalizeNonNegativeDecimal(
                        weight
                );

        scoreAwarded =
                normalizeNonNegativeDecimal(
                        scoreAwarded
                );

        if (scoreAwarded.compareTo(weight) > 0) {
            scoreAwarded = weight;
        }

        executionOrder =
                Math.max(
                        executionOrder,
                        0
                );

        if (executionDurationMs != null) {
            executionDurationMs =
                    Math.max(
                            executionDurationMs,
                            0L
                    );
        }
    }

    public void setRuleCode(
            String ruleCode) {

        this.ruleCode =
                normalizeRequiredText(
                        ruleCode,
                        "Rule code",
                        100
                );
    }

    public void setRuleName(
            String ruleName) {

        this.ruleName =
                normalizeRequiredText(
                        ruleName,
                        "Rule name",
                        200
                );
    }

    public void setRuleDescription(
            String ruleDescription) {

        this.ruleDescription =
                normalizeNullableText(
                        ruleDescription,
                        1000
                );
    }

    public void setWeight(
            BigDecimal weight) {

        this.weight =
                normalizeNonNegativeDecimal(
                        weight
                );

        if (scoreAwarded != null
                && scoreAwarded.compareTo(
                this.weight
        ) > 0) {

            scoreAwarded =
                    this.weight;
        }
    }

    public void setWeight(
            double weight) {

        setWeight(
                BigDecimal.valueOf(
                        weight
                )
        );
    }

    public void setScoreAwarded(
            BigDecimal scoreAwarded) {

        BigDecimal normalizedScore =
                normalizeNonNegativeDecimal(
                        scoreAwarded
                );

        if (weight != null
                && normalizedScore.compareTo(
                weight
        ) > 0) {

            normalizedScore =
                    weight;
        }

        this.scoreAwarded =
                normalizedScore;
    }

    public void setScoreAwarded(
            double scoreAwarded) {

        setScoreAwarded(
                BigDecimal.valueOf(
                        scoreAwarded
                )
        );
    }

    public void setMessage(
            String message) {

        this.message =
                normalizeNullableText(
                        message,
                        1000
                );
    }

    public void setExpectedValue(
            String expectedValue) {

        this.expectedValue =
                normalizeNullableText(
                        expectedValue,
                        1000
                );
    }

    public void setActualValue(
            String actualValue) {

        this.actualValue =
                normalizeNullableText(
                        actualValue,
                        1000
                );
    }

    public void setExecutionOrder(
            int executionOrder) {

        this.executionOrder =
                Math.max(
                        executionOrder,
                        0
                );
    }

    public void setExecutionDurationMs(
            Long executionDurationMs) {

        if (executionDurationMs == null) {
            this.executionDurationMs =
                    null;

            return;
        }

        this.executionDurationMs =
                Math.max(
                        executionDurationMs,
                        0L
                );
    }

    private BigDecimal normalizeNonNegativeDecimal(
            BigDecimal value) {

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return ZERO_DECIMAL;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String normalizeRequiredText(
            String value,
            String fieldName,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " is required"
            );
        }

        String normalizedValue =
                value.trim();

        return limitText(
                normalizedValue,
                maximumLength
        );
    }

    private String normalizeNullableText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return limitText(
                value.trim(),
                maximumLength
        );
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value == null) {
            return null;
        }

        if (value.length() > maximumLength) {
            return value.substring(
                    0,
                    maximumLength
            );
        }

        return value;
    }
}