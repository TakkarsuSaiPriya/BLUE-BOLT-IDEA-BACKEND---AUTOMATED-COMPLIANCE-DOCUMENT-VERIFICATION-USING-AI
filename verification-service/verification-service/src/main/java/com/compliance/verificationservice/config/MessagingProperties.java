package com.compliance.verificationservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {

    private Destinations destinations =
            new Destinations();

    private Redelivery redelivery =
            new Redelivery();

    @PostConstruct
    public void validate() {

        if (destinations == null) {
            throw new IllegalStateException(
                    "Messaging destinations must be configured"
            );
        }

        validateRequiredText(
                destinations.getOcrCompletedTopic(),
                "OCR completed topic"
        );

        validateRequiredText(
                destinations.getOcrCompletedConsumerQueue(),
                "OCR completed consumer queue"
        );

        validateRequiredText(
                destinations.getVerificationCompletedTopic(),
                "Verification completed topic"
        );

        validateRequiredText(
                destinations.getVerificationFailedTopic(),
                "Verification failed topic"
        );

        if (redelivery == null) {
            throw new IllegalStateException(
                    "Messaging redelivery settings must be configured"
            );
        }

        if (redelivery.getInitialDelay() < 0L) {
            throw new IllegalStateException(
                    "Initial redelivery delay cannot be negative"
            );
        }

        if (redelivery.getDelay() < 0L) {
            throw new IllegalStateException(
                    "Redelivery delay cannot be negative"
            );
        }

        if (redelivery.getMaximumAttempts() < 0) {
            throw new IllegalStateException(
                    "Maximum redelivery attempts cannot be negative"
            );
        }

        if (redelivery.getBackoffMultiplier() < 1.0) {
            throw new IllegalStateException(
                    "Redelivery backoff multiplier must be at least 1.0"
            );
        }
    }

    public Destinations getDestinations() {
        return destinations;
    }

    public void setDestinations(
            Destinations destinations) {

        this.destinations = destinations;
    }

    public Redelivery getRedelivery() {
        return redelivery;
    }

    public void setRedelivery(
            Redelivery redelivery) {

        this.redelivery = redelivery;
    }

    private void validateRequiredText(
            String value,
            String propertyName) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    propertyName + " must be configured"
            );
        }
    }

    public static class Destinations {

        private String ocrCompletedTopic =
                "VirtualTopic.OcrCompleted";

        private String ocrCompletedConsumerQueue =
                "Consumer.VerificationService.VirtualTopic.OcrCompleted";

        private String verificationCompletedTopic =
                "VirtualTopic.VerificationCompleted";

        private String verificationFailedTopic =
                "VirtualTopic.VerificationFailed";

        public String getOcrCompletedTopic() {
            return ocrCompletedTopic;
        }

        public void setOcrCompletedTopic(
                String ocrCompletedTopic) {

            this.ocrCompletedTopic =
                    normalize(ocrCompletedTopic);
        }

        public String getOcrCompletedConsumerQueue() {
            return ocrCompletedConsumerQueue;
        }

        public void setOcrCompletedConsumerQueue(
                String ocrCompletedConsumerQueue) {

            this.ocrCompletedConsumerQueue =
                    normalize(
                            ocrCompletedConsumerQueue
                    );
        }

        public String getVerificationCompletedTopic() {
            return verificationCompletedTopic;
        }

        public void setVerificationCompletedTopic(
                String verificationCompletedTopic) {

            this.verificationCompletedTopic =
                    normalize(
                            verificationCompletedTopic
                    );
        }

        public String getVerificationFailedTopic() {
            return verificationFailedTopic;
        }

        public void setVerificationFailedTopic(
                String verificationFailedTopic) {

            this.verificationFailedTopic =
                    normalize(
                            verificationFailedTopic
                    );
        }

        private String normalize(
                String value) {

            if (value == null || value.isBlank()) {
                return null;
            }

            return value.trim();
        }
    }

    public static class Redelivery {

        private long initialDelay = 2000L;
        private long delay = 5000L;
        private int maximumAttempts = 3;
        private boolean useExponentialBackoff = true;
        private double backoffMultiplier = 2.0;

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(
                long initialDelay) {

            this.initialDelay = initialDelay;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(
                long delay) {

            this.delay = delay;
        }

        public int getMaximumAttempts() {
            return maximumAttempts;
        }

        public void setMaximumAttempts(
                int maximumAttempts) {

            this.maximumAttempts =
                    maximumAttempts;
        }

        public boolean isUseExponentialBackoff() {
            return useExponentialBackoff;
        }

        public void setUseExponentialBackoff(
                boolean useExponentialBackoff) {

            this.useExponentialBackoff =
                    useExponentialBackoff;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(
                double backoffMultiplier) {

            this.backoffMultiplier =
                    backoffMultiplier;
        }
    }
}