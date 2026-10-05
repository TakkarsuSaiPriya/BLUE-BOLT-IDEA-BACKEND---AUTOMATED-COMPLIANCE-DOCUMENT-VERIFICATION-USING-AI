package com.compliance.verificationservice.dto.response;

import java.util.LinkedHashMap;
import java.util.Map;

public class VerificationSummaryResponse {

    private long total;
    private long pending;
    private long processing;
    private long verified;
    private long rejected;
    private long reviewRequired;
    private long failed;

    public VerificationSummaryResponse() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(
            long total) {

        this.total = total;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(
            long pending) {

        this.pending = pending;
    }

    public long getProcessing() {
        return processing;
    }

    public void setProcessing(
            long processing) {

        this.processing = processing;
    }

    public long getVerified() {
        return verified;
    }

    public void setVerified(
            long verified) {

        this.verified = verified;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(
            long rejected) {

        this.rejected = rejected;
    }

    public long getReviewRequired() {
        return reviewRequired;
    }

    public void setReviewRequired(
            long reviewRequired) {

        this.reviewRequired =
                reviewRequired;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(
            long failed) {

        this.failed = failed;
    }

    public Map<String, Long> asMap() {

        Map<String, Long> summary =
                new LinkedHashMap<>();

        summary.put("TOTAL", total);
        summary.put("PENDING", pending);
        summary.put("PROCESSING", processing);
        summary.put("VERIFIED", verified);
        summary.put("REJECTED", rejected);
        summary.put(
                "REVIEW_REQUIRED",
                reviewRequired
        );
        summary.put("FAILED", failed);

        return summary;
    }
}