package com.compliance.ocrservice.controller;

import com.compliance.ocrservice.dto.request.ManualOcrRequest;
import com.compliance.ocrservice.dto.request.OcrRetryRequest;
import com.compliance.ocrservice.dto.response.ApiResponse;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.ocr.OcrDocumentContent;
import com.compliance.ocrservice.producer.OcrEventProducer;
import com.compliance.ocrservice.service.interfaces.DocumentDownloadService;
import com.compliance.ocrservice.service.interfaces.OcrService;
import com.compliance.ocrservice.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocr")
@Validated
@Tag(
        name = "OCR Processing",
        description = "Start and retry OCR processing operations"
)
@SecurityRequirement(name = "bearerAuth")
public class OcrCommandController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OcrCommandController.class
            );

    private final OcrService ocrService;
    private final DocumentDownloadService
            documentDownloadService;
    private final OcrEventProducer ocrEventProducer;
    private final SecurityContextUtil securityContextUtil;

    public OcrCommandController(
            OcrService ocrService,
            DocumentDownloadService documentDownloadService,
            OcrEventProducer ocrEventProducer,
            SecurityContextUtil securityContextUtil) {

        this.ocrService = ocrService;
        this.documentDownloadService =
                documentDownloadService;
        this.ocrEventProducer =
                ocrEventProducer;
        this.securityContextUtil =
                securityContextUtil;
    }

    @PostMapping("/process")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER')"
    )
    @Operation(
            summary = "Start OCR processing",
            description = "Downloads a document from Document Service "
                    + "and performs OCR processing"
    )
    public ResponseEntity<ApiResponse<OcrResultResponse>>
    processDocument(
            @Valid
            @RequestBody
            ManualOcrRequest request) {

        Long documentId =
                request.getDocumentId();

        String requestedBy =
                securityContextUtil
                        .getCurrentUsername();

        OcrDocumentContent documentContent =
                null;

        try {
            LOGGER.info(
                    "Manual OCR request received. "
                            + "documentId={}, requestedBy={}, force={}",
                    documentId,
                    requestedBy,
                    request.isForceReprocess()
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

            OcrResultResponse result =
                    ocrService.processDocument(
                            documentContent,
                            request.getLanguage(),
                            requestedBy,
                            request.isForceReprocess()
                    );

            documentDownloadService
                    .markOcrCompleted(
                            documentId,
                            buildCompletionReason(result)
                    );

            publishCompletedEventSafely(
                    result
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "OCR processing completed successfully",
                            result
                    )
            );

        } catch (RuntimeException exception) {

            handleProcessingFailure(
                    documentId,
                    documentContent,
                    requestedBy,
                    exception
            );

            throw exception;
        }
    }

    @PostMapping("/results/{ocrResultId}/retry")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'VERIFIER')"
    )
    @Operation(
            summary = "Retry OCR processing",
            description = "Retries a failed or low-confidence OCR result"
    )
    public ResponseEntity<ApiResponse<OcrResultResponse>>
    retryOcr(
            @PathVariable("ocrResultId")
            @Positive(
                    message = "OCR result ID must be positive"
            )
            Long ocrResultId,
            @Valid
            @RequestBody
            OcrRetryRequest request) {

        String requestedBy =
                securityContextUtil
                        .getCurrentUsername();

        OcrResultResponse existingResult =
                ocrService.getResultById(
                        ocrResultId
                );

        Long documentId =
                existingResult.getDocumentId();

        OcrDocumentContent documentContent =
                null;

        try {
            LOGGER.info(
                    "OCR retry request received. "
                            + "ocrResultId={}, documentId={}, requestedBy={}",
                    ocrResultId,
                    documentId,
                    requestedBy
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

            OcrResultResponse result =
                    ocrService.retryOcr(
                            ocrResultId,
                            documentContent,
                            request.getLanguage(),
                            requestedBy,
                            request.getReason()
                    );

            documentDownloadService
                    .markOcrCompleted(
                            documentId,
                            buildCompletionReason(result)
                    );

            publishCompletedEventSafely(
                    result
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "OCR retry completed successfully",
                            result
                    )
            );

        } catch (RuntimeException exception) {

            handleProcessingFailure(
                    documentId,
                    documentContent,
                    requestedBy,
                    exception
            );

            throw exception;
        }
    }

    private void handleProcessingFailure(
            Long documentId,
            OcrDocumentContent documentContent,
            String requestedBy,
            RuntimeException exception) {

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
                    "Unable to mark document OCR status "
                            + "as FAILED. documentId={}",
                    documentId,
                    statusException
            );
        }

        try {
            ocrEventProducer.publishOcrFailed(
                    documentId,
                    findLatestResultId(documentId),
                    resolveOriginalFileName(
                            documentContent
                    ),
                    requestedBy,
                    errorMessage,
                    1,
                    false
            );

        } catch (RuntimeException eventException) {

            LOGGER.error(
                    "Unable to publish manual OCR failure event. "
                            + "documentId={}",
                    documentId,
                    eventException
            );
        }

        LOGGER.error(
                "Manual OCR operation failed. "
                        + "documentId={}, requestedBy={}",
                documentId,
                requestedBy,
                exception
        );
    }

    private Long findLatestResultId(
            Long documentId) {

        if (documentId == null
                || documentId <= 0) {

            return null;
        }

        try {
            return ocrService
                    .getLatestResultByDocumentId(
                            documentId
                    )
                    .getId();

        } catch (RuntimeException ignored) {

            return null;
        }
    }

    private void publishCompletedEventSafely(
            OcrResultResponse result) {

        try {
            ocrEventProducer
                    .publishOcrCompleted(
                            result
                    );

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "OCR completed successfully, but "
                            + "completion event publication failed. "
                            + "documentId={}, ocrResultId={}",
                    result.getDocumentId(),
                    result.getId(),
                    exception
            );
        }
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
            OcrDocumentContent documentContent) {

        if (documentContent == null
                || documentContent
                .getOriginalFileName() == null
                || documentContent
                .getOriginalFileName()
                .isBlank()) {

            return "document";
        }

        return documentContent
                .getOriginalFileName();
    }

    private String safeErrorMessage(
            Throwable exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return "OCR processing failed";
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