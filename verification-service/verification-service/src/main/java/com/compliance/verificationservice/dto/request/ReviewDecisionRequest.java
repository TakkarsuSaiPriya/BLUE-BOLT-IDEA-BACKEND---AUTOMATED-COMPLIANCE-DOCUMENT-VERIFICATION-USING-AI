package com.compliance.verificationservice.dto.request;

import com.compliance.verificationservice.enums.ReviewDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewDecisionRequest {

    @NotNull(
            message = "Review decision is required"
    )
    private ReviewDecision decision;

    @Size(
            max = 1000,
            message = "Review reason cannot exceed 1000 characters"
    )
    private String reason;

    public ReviewDecisionRequest() {
    }

    public ReviewDecision getDecision() {
        return decision;
    }

    public void setDecision(
            ReviewDecision decision) {

        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason =
                reason == null
                        ? null
                        : reason.trim();
    }
}