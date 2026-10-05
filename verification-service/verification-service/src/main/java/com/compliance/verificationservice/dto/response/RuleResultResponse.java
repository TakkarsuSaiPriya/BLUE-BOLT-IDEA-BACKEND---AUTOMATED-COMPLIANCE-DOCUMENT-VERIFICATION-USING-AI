package com.compliance.verificationservice.dto.response;

import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;

import java.math.BigDecimal;

public class RuleResultResponse {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleDescription;
    private RuleCategory category;
    private RuleOutcome outcome;
    private boolean mandatory;
    private BigDecimal weight;
    private BigDecimal scoreAwarded;
    private String message;
    private String expectedValue;
    private String actualValue;
    private int executionOrder;
    private Long executionDurationMs;

    public RuleResultResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(
            String ruleCode) {

        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(
            String ruleName) {

        this.ruleName = ruleName;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(
            String ruleDescription) {

        this.ruleDescription =
                ruleDescription;
    }

    public RuleCategory getCategory() {
        return category;
    }

    public void setCategory(
            RuleCategory category) {

        this.category = category;
    }

    public RuleOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(
            RuleOutcome outcome) {

        this.outcome = outcome;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(
            boolean mandatory) {

        this.mandatory = mandatory;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(
            BigDecimal weight) {

        this.weight = weight;
    }

    public BigDecimal getScoreAwarded() {
        return scoreAwarded;
    }

    public void setScoreAwarded(
            BigDecimal scoreAwarded) {

        this.scoreAwarded =
                scoreAwarded;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(
            String expectedValue) {

        this.expectedValue =
                expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(
            String actualValue) {

        this.actualValue = actualValue;
    }

    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(
            int executionOrder) {

        this.executionOrder =
                executionOrder;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(
            Long executionDurationMs) {

        this.executionDurationMs =
                executionDurationMs;
    }
}