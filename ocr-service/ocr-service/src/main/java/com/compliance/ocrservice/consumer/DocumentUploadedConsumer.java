package com.compliance.ocrservice.consumer;

import com.compliance.ocrservice.config.ActiveMQConfig;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.event.DocumentUploadedEvent;
import com.compliance.ocrservice.producer.OcrEventProducer;
import com.compliance.ocrservice.ocr.OcrDocumentContent;
import com.compliance.ocrservice.service.interfaces.DocumentDownloadService;
import com.compliance.ocrservice.service.interfaces.OcrService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadedConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DocumentUploadedConsumer.class
            );

    private static final String DELIVERY_COUNT_PROPERTY =
            "JMSXDeliveryCount";

    private final ObjectMapper objectMapper;
    private final DocumentDownloadService
            documentDownloadService;
    private final OcrService ocrService;
    private final OcrEventProducer ocrEventProducer;

    public DocumentUploadedConsumer(
            ObjectMapper objectMapper,
            DocumentDownloadService documentDownloadService,
            OcrService ocrService,
            OcrEventProducer ocrEventProducer) {

        this.objectMapper = objectMapper;
        this.documentDownloadService =
                documentDownloadService;
        this.ocrService = ocrService;
        this.ocrEventProducer =
                ocrEventProducer;
    }

    @JmsListener(
            destination = ActiveMQConfig.OCR_CONSUMER_QUEUE,
            containerFactory = "ocrJmsListenerContainerFactory"
    )
    public void consumeDocumentUploaded(
            Message message) {

        DocumentUploadedEvent event =
                deserializeEvent(message);

        validateEvent(event);

        int deliveryAttempt =
                resolveDeliveryAttempt(message);

        Long documentId =
                event.getDocumentId();

        String requestedBy =
                normalizeRequestedBy(
                        event.getUploadedBy()
                );

        OcrDocumentContent documentContent =
                null;

        OcrResultResponse ocrResult =
                null;

        try {
            LOGGER.info(
                    "Received document uploaded event. "
                            + "documentId={}, eventId={}, attempt={}",
                    documentId,
                    event.getEventId(),
                    deliveryAttempt
            );

            documentDownloadService
                    .markOcrProcessing(
                            documentId
                    );

            documentContent =
                    documentDownloadService
                            .downloadDocument(
                                    documentId
                            );

            boolean forceReprocess =
                    deliveryAttempt > 1;

            ocrResult =
                    ocrService.processDocument(
                            documentContent,
                            null,
                            requestedBy,
                            forceReprocess
                    );

            documentDownloadService
                    .markOcrCompleted(
                            documentId,
                            buildCompletionReason(
                                    ocrResult
                            )
                    );

            ocrEventProducer
                    .publishOcrCompleted(
                            ocrResult
                    );

            LOGGER.info(
                    "Automatic OCR completed. "
                            + "documentId={}, ocrResultId={}, status={}",
                    documentId,
                    ocrResult.getId(),
                    ocrResult.getStatus()
            );

        } catch (RuntimeException exception) {

            String errorMessage =
                    safeErrorMessage(exception);

            try {
                documentDownloadService
                        .markOcrFailed(
                                documentId,
                                errorMessage
                        );

            } catch (RuntimeException statusException) {

                LOGGER.error(
                        "Unable to mark document OCR failure. "
                                + "documentId={}",
                        documentId,
                        statusException
                );
            }

            try {
                ocrEventProducer
                        .publishOcrFailed(
                                documentId,
                                ocrResult == null
                                        ? null
                                        : ocrResult.getId(),
                                resolveOriginalFileName(
                                        event,
                                        documentContent
                                ),
                                requestedBy,
                                errorMessage,
                                deliveryAttempt,
                                true
                        );

            } catch (RuntimeException publishException) {

                LOGGER.error(
                        "Unable to publish OCR failed event. "
                                + "documentId={}",
                        documentId,
                        publishException
                );
            }

            LOGGER.error(
                    "Automatic OCR processing failed. "
                            + "documentId={}, attempt={}",
                    documentId,
                    deliveryAttempt,
                    exception
            );

            throw new IllegalStateException(
                    "Automatic OCR processing failed "
                            + "for document ID: "
                            + documentId,
                    exception
            );
        }
    }

    private DocumentUploadedEvent deserializeEvent(
            Message message) {

        if (message == null) {
            throw new IllegalArgumentException(
                    "Document upload JMS message is required"
            );
        }

        try {
            if (!(message
                    instanceof TextMessage textMessage)) {

                throw new IllegalArgumentException(
                        "Document upload event must be "
                                + "a JSON text message"
                );
            }

            String json =
                    textMessage.getText();

            if (json == null
                    || json.isBlank()) {

                throw new IllegalArgumentException(
                        "Document upload event payload is empty"
                );
            }

            return objectMapper.readValue(
                    json,
                    DocumentUploadedEvent.class
            );

        } catch (JMSException exception) {

            throw new IllegalStateException(
                    "Unable to read document upload "
                            + "JMS message",
                    exception
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalArgumentException(
                    "Document upload event contains "
                            + "invalid JSON",
                    exception
            );
        }
    }

    private void validateEvent(
            DocumentUploadedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Document upload event is required"
            );
        }

        if (event.getDocumentId() == null
                || event.getDocumentId() <= 0) {

            throw new IllegalArgumentException(
                    "Document upload event contains "
                            + "an invalid document ID"
            );
        }
    }

    private int resolveDeliveryAttempt(
            Message message) {

        try {
            if (message.propertyExists(
                    DELIVERY_COUNT_PROPERTY)) {

                return Math.max(
                        message.getIntProperty(
                                DELIVERY_COUNT_PROPERTY
                        ),
                        1
                );
            }

        } catch (JMSException exception) {

            LOGGER.warn(
                    "Unable to read JMS delivery count",
                    exception
            );
        }

        return 1;
    }

    private String normalizeRequestedBy(
            String uploadedBy) {

        if (uploadedBy == null
                || uploadedBy.isBlank()) {

            return "system";
        }

        return uploadedBy.trim()
                .toLowerCase();
    }

    private String buildCompletionReason(
            OcrResultResponse result) {

        if (result == null
                || result.getAverageConfidence() == null) {

            return "OCR text extraction completed";
        }

        return "OCR text extraction completed "
                + "with average confidence "
                + result.getAverageConfidence();
    }

    private String resolveOriginalFileName(
            DocumentUploadedEvent event,
            OcrDocumentContent documentContent) {

        if (documentContent != null
                && documentContent
                .getOriginalFileName() != null
                && !documentContent
                .getOriginalFileName()
                .isBlank()) {

            return documentContent
                    .getOriginalFileName();
        }

        if (event != null
                && event.getOriginalFileName() != null
                && !event.getOriginalFileName()
                .isBlank()) {

            return event.getOriginalFileName();
        }

        return "document";
    }

    private String safeErrorMessage(
            Throwable exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return "Automatic OCR processing failed";
        }

        String message =
                exception.getMessage().trim();

        if (message.length() > 500) {
            return message.substring(
                    0,
                    500
            );
        }

        return message;
    }
}