package com.compliance.verificationservice.producer;

import com.compliance.verificationservice.config.MessagingProperties;
import com.compliance.verificationservice.dto.event.VerificationCompletedEvent;
import com.compliance.verificationservice.dto.event.VerificationFailedEvent;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Component
public class VerificationEventProducerImpl
        implements VerificationEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    VerificationEventProducerImpl.class
            );

    private final JmsTemplate topicJmsTemplate;

    private final MessagingProperties
            messagingProperties;

    public VerificationEventProducerImpl(
            @Qualifier("verificationTopicJmsTemplate")
            JmsTemplate topicJmsTemplate,
            MessagingProperties messagingProperties) {

        this.topicJmsTemplate =
                topicJmsTemplate;

        this.messagingProperties =
                messagingProperties;
    }

    @Override
    public void publishVerificationCompleted(
            VerificationResultResponse result) {

        validateCompletedResult(
                result
        );

        VerificationCompletedEvent event =
                new VerificationCompletedEvent();

        event.setEventId(
                UUID.randomUUID().toString()
        );

        event.setEventType(
                "VERIFICATION_COMPLETED"
        );

        event.setVerificationResultId(
                result.getId()
        );

        event.setDocumentId(
                result.getDocumentId()
        );

        event.setOcrResultId(
                result.getOcrResultId()
        );

        event.setStatus(
                result.getStatus()
        );

        event.setVerificationScore(
                result.getVerificationScore()
        );

        event.setMandatoryFailureCount(
                result.getMandatoryFailureCount()
        );

        event.setRequestedBy(
                normalizeUsername(
                        result.getRequestedBy()
                )
        );

        event.setOccurredAt(
                LocalDateTime.now()
        );

        String destination =
                messagingProperties
                        .getDestinations()
                        .getVerificationCompletedTopic();

        topicJmsTemplate.convertAndSend(
                destination,
                event
        );

        LOGGER.info(
                "Published verification-completed event. "
                        + "eventId={}, verificationResultId={}, "
                        + "documentId={}, status={}",
                event.getEventId(),
                event.getVerificationResultId(),
                event.getDocumentId(),
                event.getStatus()
        );
    }

    @Override
    public void publishVerificationFailed(
            Long verificationResultId,
            Long documentId,
            Long ocrResultId,
            String failureReason,
            String requestedBy) {

        if (documentId == null
                || documentId <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid document ID is required "
                            + "for verification-failed event"
            );
        }

        if (ocrResultId == null
                || ocrResultId <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid OCR result ID is required "
                            + "for verification-failed event"
            );
        }

        VerificationFailedEvent event =
                new VerificationFailedEvent();

        event.setEventId(
                UUID.randomUUID().toString()
        );

        event.setEventType(
                "VERIFICATION_FAILED"
        );

        event.setVerificationResultId(
                verificationResultId
        );

        event.setDocumentId(
                documentId
        );

        event.setOcrResultId(
                ocrResultId
        );

        event.setFailureReason(
                normalizeFailureReason(
                        failureReason
                )
        );

        event.setRequestedBy(
                normalizeUsername(
                        requestedBy
                )
        );

        event.setOccurredAt(
                LocalDateTime.now()
        );

        String destination =
                messagingProperties
                        .getDestinations()
                        .getVerificationFailedTopic();

        topicJmsTemplate.convertAndSend(
                destination,
                event
        );

        LOGGER.info(
                "Published verification-failed event. "
                        + "eventId={}, verificationResultId={}, "
                        + "documentId={}, ocrResultId={}",
                event.getEventId(),
                event.getVerificationResultId(),
                event.getDocumentId(),
                event.getOcrResultId()
        );
    }

    private void validateCompletedResult(
            VerificationResultResponse result) {

        if (result == null) {
            throw new InvalidVerificationRequestException(
                    "Verification result is required "
                            + "for completion event"
            );
        }

        if (result.getId() == null
                || result.getId() <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid verification result ID is required"
            );
        }

        if (result.getDocumentId() == null
                || result.getDocumentId() <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid document ID is required"
            );
        }

        if (result.getOcrResultId() == null
                || result.getOcrResultId() <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid OCR result ID is required"
            );
        }

        if (result.getStatus() == null) {
            throw new InvalidVerificationRequestException(
                    "Verification status is required"
            );
        }
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "verification-service";
        }

        String normalized =
                username.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return limitText(
                normalized,
                150
        );
    }

    private String normalizeFailureReason(
            String failureReason) {

        if (failureReason == null
                || failureReason.isBlank()) {

            return "Verification processing failed";
        }

        return limitText(
                failureReason.trim(),
                2000
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
