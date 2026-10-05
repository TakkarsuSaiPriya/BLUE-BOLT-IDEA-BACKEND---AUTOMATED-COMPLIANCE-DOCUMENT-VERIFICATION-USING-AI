package com.compliance.ocrservice.producer;

import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.event.OcrCompletedEvent;
import com.compliance.ocrservice.event.OcrFailedEvent;
import jakarta.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OcrEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OcrEventProducer.class
            );

    private static final String OCR_COMPLETED_EVENT_TYPE =
            "OCR_COMPLETED";

    private static final String OCR_FAILED_EVENT_TYPE =
            "OCR_FAILED";

    private final JmsTemplate jmsTemplate;
    private final Topic ocrCompletedTopic;
    private final Topic ocrFailedTopic;

    public OcrEventProducer(
            JmsTemplate jmsTemplate,
            @Qualifier("ocrCompletedTopic")
            Topic ocrCompletedTopic,
            @Qualifier("ocrFailedTopic")
            Topic ocrFailedTopic) {

        this.jmsTemplate = jmsTemplate;
        this.ocrCompletedTopic =
                ocrCompletedTopic;
        this.ocrFailedTopic =
                ocrFailedTopic;
    }

    public void publishOcrCompleted(
            OcrResultResponse result) {

        if (result == null) {
            throw new IllegalArgumentException(
                    "OCR result is required "
                            + "for completion event"
            );
        }

        OcrCompletedEvent event =
                createCompletedEvent(result);

        try {
            jmsTemplate.convertAndSend(
                    ocrCompletedTopic,
                    event,
                    message -> {
                        message.setStringProperty(
                                "eventType",
                                OCR_COMPLETED_EVENT_TYPE
                        );

                        message.setStringProperty(
                                "eventId",
                                event.getEventId()
                        );

                        message.setLongProperty(
                                "documentId",
                                result.getDocumentId()
                        );

                        return message;
                    }
            );

            LOGGER.info(
                    "Published OCR completed event. "
                            + "documentId={}, ocrResultId={}",
                    result.getDocumentId(),
                    result.getId()
            );

        } catch (JmsException exception) {

            throw new IllegalStateException(
                    "Unable to publish OCR completed event "
                            + "for document ID: "
                            + result.getDocumentId(),
                    exception
            );
        }
    }

    public void publishOcrFailed(
            Long documentId,
            Long ocrResultId,
            String originalFileName,
            String requestedBy,
            String errorMessage,
            int deliveryAttempt,
            boolean retryable) {

        OcrFailedEvent event =
                createFailedEvent(
                        documentId,
                        ocrResultId,
                        originalFileName,
                        requestedBy,
                        errorMessage,
                        deliveryAttempt,
                        retryable
                );

        try {
            jmsTemplate.convertAndSend(
                    ocrFailedTopic,
                    event,
                    message -> {
                        message.setStringProperty(
                                "eventType",
                                OCR_FAILED_EVENT_TYPE
                        );

                        message.setStringProperty(
                                "eventId",
                                event.getEventId()
                        );

                        if (documentId != null) {
                            message.setLongProperty(
                                    "documentId",
                                    documentId
                            );
                        }

                        return message;
                    }
            );

            LOGGER.warn(
                    "Published OCR failed event. "
                            + "documentId={}, attempt={}, retryable={}",
                    documentId,
                    deliveryAttempt,
                    retryable
            );

        } catch (JmsException exception) {

            throw new IllegalStateException(
                    "Unable to publish OCR failed event "
                            + "for document ID: "
                            + documentId,
                    exception
            );
        }
    }

    private OcrCompletedEvent createCompletedEvent(
            OcrResultResponse result) {

        OcrCompletedEvent event =
                new OcrCompletedEvent();

        event.setEventId(
                UUID.randomUUID().toString()
        );

        event.setEventType(
                OCR_COMPLETED_EVENT_TYPE
        );

        event.setDocumentId(
                result.getDocumentId()
        );

        event.setOcrResultId(
                result.getId()
        );

        event.setOriginalFileName(
                result.getOriginalFileName()
        );

        event.setStatus(
                result.getStatus() == null
                        ? null
                        : result.getStatus().name()
        );

        event.setLanguage(
                result.getLanguage()
        );

        event.setExtractedText(
                result.getExtractedText()
        );

        event.setAverageConfidence(
                result.getAverageConfidence()
        );

        event.setPageCount(
                result.getPageCount()
        );

        event.setCharacterCount(
                result.getCharacterCount()
        );

        event.setWordCount(
                result.getWordCount()
        );

        event.setProcessingDurationMs(
                result.getProcessingDurationMs()
        );

        event.setRequestedBy(
                result.getRequestedBy()
        );

        event.setCompletedAt(
                result.getCompletedAt()
        );

        event.setOccurredAt(
                LocalDateTime.now()
        );

        return event;
    }

    private OcrFailedEvent createFailedEvent(
            Long documentId,
            Long ocrResultId,
            String originalFileName,
            String requestedBy,
            String errorMessage,
            int deliveryAttempt,
            boolean retryable) {

        OcrFailedEvent event =
                new OcrFailedEvent();

        event.setEventId(
                UUID.randomUUID().toString()
        );

        event.setEventType(
                OCR_FAILED_EVENT_TYPE
        );

        event.setDocumentId(documentId);
        event.setOcrResultId(ocrResultId);

        event.setOriginalFileName(
                originalFileName
        );

        event.setRequestedBy(
                requestedBy
        );

        event.setErrorMessage(
                limitErrorMessage(errorMessage)
        );

        event.setDeliveryAttempt(
                Math.max(deliveryAttempt, 1)
        );

        event.setRetryable(retryable);

        event.setFailedAt(
                LocalDateTime.now()
        );

        event.setOccurredAt(
                LocalDateTime.now()
        );

        return event;
    }

    private String limitErrorMessage(
            String errorMessage) {

        if (errorMessage == null
                || errorMessage.isBlank()) {

            return "OCR processing failed";
        }

        String normalizedMessage =
                errorMessage.trim();

        if (normalizedMessage.length() > 2000) {
            return normalizedMessage.substring(
                    0,
                    2000
            );
        }

        return normalizedMessage;
    }
}