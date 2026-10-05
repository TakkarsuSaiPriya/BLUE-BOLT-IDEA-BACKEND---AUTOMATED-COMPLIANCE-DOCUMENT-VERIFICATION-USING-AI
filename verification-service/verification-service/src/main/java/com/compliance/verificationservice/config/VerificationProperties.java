package com.compliance.verificationservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "verification"
)
public class VerificationProperties {

    private double minimumOcrConfidence =
            40.0;

    private double verifiedScoreThreshold =
            80.0;

    private double reviewScoreThreshold =
            50.0;

    private int maximumRetryCount =
            3;

    @PostConstruct
    public void validate() {

        validatePercentage(
                minimumOcrConfidence,
                "Minimum OCR confidence"
        );

        validatePercentage(
                verifiedScoreThreshold,
                "Verified score threshold"
        );

        validatePercentage(
                reviewScoreThreshold,
                "Review score threshold"
        );

        if (reviewScoreThreshold
                > verifiedScoreThreshold) {

            throw new IllegalStateException(
                    "Review score threshold cannot be "
                            + "greater than verified score threshold"
            );
        }

        if (maximumRetryCount < 0) {
            throw new IllegalStateException(
                    "Maximum retry count cannot be negative"
            );
        }
    }

    public double getMinimumOcrConfidence() {
        return minimumOcrConfidence;
    }

    public void setMinimumOcrConfidence(
            double minimumOcrConfidence) {

        this.minimumOcrConfidence =
                minimumOcrConfidence;
    }

    public double getVerifiedScoreThreshold() {
        return verifiedScoreThreshold;
    }

    public void setVerifiedScoreThreshold(
            double verifiedScoreThreshold) {

        this.verifiedScoreThreshold =
                verifiedScoreThreshold;
    }

    public double getReviewScoreThreshold() {
        return reviewScoreThreshold;
    }

    public void setReviewScoreThreshold(
            double reviewScoreThreshold) {

        this.reviewScoreThreshold =
                reviewScoreThreshold;
    }

    public int getMaximumRetryCount() {
        return maximumRetryCount;
    }

    public void setMaximumRetryCount(
            int maximumRetryCount) {

        this.maximumRetryCount =
                maximumRetryCount;
    }

    private void validatePercentage(
            double value,
            String propertyName) {

        if (value < 0.0
                || value > 100.0) {

            throw new IllegalStateException(
                    propertyName
                            + " must be between 0 and 100"
            );
        }
    }
}