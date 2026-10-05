package com.compliance.verificationservice.rule;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;

import java.math.BigDecimal;

public interface VerificationRule {

    String getCode();

    String getName();

    String getDescription();

    RuleCategory getCategory();

    BigDecimal getWeight();

    boolean isMandatory();

    int getExecutionOrder();

    default boolean supports(
            VerificationContext context) {

        return context != null;
    }

    VerificationRuleResult evaluate(
            VerificationContext context
    );
}