package com.compliance.verificationservice.consumer;

import com.compliance.verificationservice.dto.event.OcrCompletedEvent;
import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.producer.VerificationEventProducer;
import com.compliance.verificationservice.repository.VerificationResultRepository;
import com.compliance.verificationservice.service.interfaces.DocumentStatusService;
import com.compliance.verificationservice.service.interfaces.OcrResultService;
import com.compliance.verificationservice.service.interfaces.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OcrCompletedEventConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OcrCompletedEventConsumer.class
            );

    private static final String DEFAULT_DOCUMENT_TYPE =
            "GENERAL";

    private final VerificationService
            verificationService;

    private final OcrResultService
            ocrResultService;

    private final DocumentStatusService
            documentStatusService;

    private final VerificationEventProducer
            verificationEventProducer;

    private final VerificationResultRepository
            verificationResultRepository;

    public OcrCompletedEventConsumer(
            VerificationService verificationService,
            OcrResultService ocrResultService,
            DocumentStatusService documentStatusService,
            VerificationEventProducer
                    verificationEventProducer,
            VerificationResultRepository
                    verificationResultRepository) {

        this.verificationService =
                verificationService;

        this.ocrResultService =
                ocrResultService;

        this.documentStatusService =
                documentStatusService;

        this.verificationEventProducer =
                verificationEventProducer;

        this.verificationResultRepository =
                verificationResultRepository;
    }

    @JmsListener(
            destination =
                    "${messaging.destinations.ocr-completed-consumer-queue}",
            containerFactory =
                    "verificationJmsListenerContainerFactory"
    )
    public void consume(
            OcrCompletedEvent event) {

        validateEvent(
                event
        );

        LOGGER.info(
                "Received OCR-completed event. "
                        + "eventId={}, ocrResultId={}, documentId={}",
                event.getEventId(),
                event.getOcrResultId(),
                event.getDocumentId()
        );

        if (verificationResultRepository
                .existsByOcrResultIdAndActiveTrue(
                        event.getOcrResultId()
                )) {

            LOGGER.info(
                    "Skipping duplicate OCR-completed event. "
                            + "eventId={}, ocrResultId={}",
                    event.getEventId(),
                    event.getOcrResultId()
            );

            return;
        }

        String requestedBy =
                normalizeUsername(
                        event.getRequestedBy()
                );

        Long verificationResultId = null;

        try {

            OcrResultInternalResponse ocrResult =
                    ocrResultService.getOcrResult(
                            event.getOcrResultId()
                    );

            validateOcrResultAgainstEvent(
                    event,
                    ocrResult
            );

            safelyMarkProcessing(
                    event.getDocumentId(),
                    requestedBy
            );

            VerificationResultResponse result =
                    verificationService
                            .processVerification(
                                    ocrResult,
                                    DEFAULT_DOCUMENT_TYPE,
                                    requestedBy,
                                    false
                            );

            verificationResultId =
                    result.getId();

            verificationEventProducer
                    .publishVerificationCompleted(
                            result
                    );

            safelyMarkCompleted(
                    result,
                    requestedBy
            );

            LOGGER.info(
                    "OCR event processed successfully. "
                            + "eventId={}, verificationResultId={}, "
                            + "documentId={}, status={}, score={}",
                    event.getEventId(),
                    result.getId(),
                    result.getDocumentId(),
                    result.getStatus(),
                    result.getVerificationScore()
            );

        } catch (Exception exception) {

            String failureReason =
                    extractFailureReason(
                            exception
                    );

            safelyPublishFailure(
                    verificationResultId,
                    event.getDocumentId(),
                    event.getOcrResultId(),
                    failureReason,
                    requestedBy
            );

            safelyMarkFailed(
                    event.getDocumentId(),
                    failureReason,
                    requestedBy
            );

            LOGGER.error(
                    "OCR-completed event processing failed. "
                            + "eventId={}, ocrResultId={}, "
                            + "documentId={}, reason={}",
                    event.getEventId(),
                    event.getOcrResultId(),
                    event.getDocumentId(),
                    failureReason,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to process OCR-completed event "
                            + event.getEventId(),
                    exception
            );
        }
    }

    private void validateEvent(
            OcrCompletedEvent event) {

        if (event == null) {
            throw new InvalidVerificationRequestException(
                    "OCR-completed event is required"
            );
        }

        if (event.getEventId() == null
                || event.getEventId().isBlank()) {

            throw new InvalidVerificationRequestException(
                    "OCR-completed event ID is required"
            );
        }

        if (event.getOcrResultId() == null
                || event.getOcrResultId() <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid OCR result ID is required"
            );
        }

        if (event.getDocumentId() == null
                || event.getDocumentId() <= 0) {

            throw new InvalidVerificationRequestException(
                    "Valid document ID is required"
            );
        }

        if (event.getStatus() == null
                || event.getStatus().isBlank()) {

            throw new InvalidVerificationRequestException(
                    "OCR event status is required"
            );
        }

        String normalizedStatus =
                event.getStatus()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!"COMPLETED".equals(normalizedStatus)
                && !"LOW_CONFIDENCE".equals(
                normalizedStatus)) {

            throw new InvalidVerificationRequestException(
                    "OCR-completed event has unsupported status: "
                            + normalizedStatus
            );
        }
    }

    private void validateOcrResultAgainstEvent(
            OcrCompletedEvent event,
            OcrResultInternalResponse ocrResult) {

        if (ocrResult == null) {
            throw new InvalidVerificationRequestException(
                    "OCR Service returned no OCR result"
            );
        }

        if (!event.getOcrResultId()
                .equals(ocrResult.getId())) {

            throw new InvalidVerificationRequestException(
                    "OCR result ID does not match the event"
            );
        }

        if (!event.getDocumentId()
                .equals(ocrResult.getDocumentId())) {

            throw new InvalidVerificationRequestException(
                    "OCR document ID does not match the event"
            );
        }
    }

    private void safelyMarkProcessing(
            Long documentId,
            String requestedBy) {

        try {

            documentStatusService
                    .markVerificationProcessing(
                            documentId,
                            requestedBy
                    );

        } catch (Exception exception) {

            LOGGER.warn(
                    "Unable to mark document {} as "
                            + "verification-processing: {}",
                    documentId,
                    exception.getMessage()
            );
        }
    }

    private void safelyMarkCompleted(
            VerificationResultResponse result,
            String requestedBy) {

        try {

            documentStatusService
                    .markVerificationCompleted(
                            result.getDocumentId(),
                            result.getStatus(),
                            result.getSummary(),
                            requestedBy
                    );

        } catch (Exception exception) {

            LOGGER.warn(
                    "Verification completed, but document status "
                            + "update failed. documentId={}, reason={}",
                    result.getDocumentId(),
                    exception.getMessage()
            );
        }
    }

    private void safelyMarkFailed(
            Long documentId,
            String failureReason,
            String requestedBy) {

        try {

            documentStatusService
                    .markVerificationFailed(
                            documentId,
                            failureReason,
                            requestedBy
                    );

        } catch (Exception exception) {

            LOGGER.warn(
                    "Unable to mark document {} as "
                            + "verification-failed: {}",
                    documentId,
                    exception.getMessage()
            );
        }
    }

    private void safelyPublishFailure(
            Long verificationResultId,
            Long documentId,
            Long ocrResultId,
            String failureReason,
            String requestedBy) {

        try {

            verificationEventProducer
                    .publishVerificationFailed(
                            verificationResultId,
                            documentId,
                            ocrResultId,
                            failureReason,
                            requestedBy
                    );

        } catch (Exception exception) {

            LOGGER.error(
                    "Unable to publish verification-failed event. "
                            + "documentId={}, ocrResultId={}",
                    documentId,
                    ocrResultId,
                    exception
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

        if (normalized.length() > 150) {
            return normalized.substring(
                    0,
                    150
            );
        }

        return normalized;
    }

    private String extractFailureReason(
            Exception exception) {

        if (exception == null) {
            return "Unknown verification processing failure";
        }

        String message =
                exception.getMessage();

        if (message == null
                || message.isBlank()) {

            message =
                    exception.getClass()
                            .getSimpleName();
        }

        String normalized =
                message.trim();

        if (normalized.length() > 2000) {
            return normalized.substring(
                    0,
                    2000
            );
        }

        return normalized;
    }
}