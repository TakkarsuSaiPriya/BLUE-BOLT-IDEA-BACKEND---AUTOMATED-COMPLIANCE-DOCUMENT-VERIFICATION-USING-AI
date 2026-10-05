package com.compliance.ocrservice.service.impl;

import com.compliance.ocrservice.config.OcrProperties;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.dto.response.OcrSummaryResponse;
import com.compliance.ocrservice.entity.OcrResult;
import com.compliance.ocrservice.enums.OcrEngine;
import com.compliance.ocrservice.enums.OcrStatus;
import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.exception.OcrProcessingException;
import com.compliance.ocrservice.exception.OcrResultNotFoundException;
import com.compliance.ocrservice.mapper.OcrResultMapper;
import com.compliance.ocrservice.ocr.OcrDocumentContent;
import com.compliance.ocrservice.ocr.OcrProcessingResult;
import com.compliance.ocrservice.ocr.OcrProcessor;
import com.compliance.ocrservice.ocr.OcrProcessorResolver;
import com.compliance.ocrservice.repository.OcrResultRepository;
import com.compliance.ocrservice.service.interfaces.OcrService;
import com.compliance.ocrservice.util.ConfidenceUtil;
import com.compliance.ocrservice.util.DocumentContentUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class OcrServiceImpl
        implements OcrService {

    private static final List<OcrStatus>
            ACTIVE_PROCESSING_STATUSES =
            List.of(
                    OcrStatus.PENDING,
                    OcrStatus.DOWNLOADING,
                    OcrStatus.PROCESSING,
                    OcrStatus.RETRY_PENDING
            );

    private final OcrResultRepository
            ocrResultRepository;

    private final OcrResultMapper
            ocrResultMapper;

    private final OcrProcessorResolver
            ocrProcessorResolver;

    private final DocumentContentUtil
            documentContentUtil;

    private final ConfidenceUtil
            confidenceUtil;

    private final OcrProperties
            ocrProperties;

    public OcrServiceImpl(
            OcrResultRepository ocrResultRepository,
            OcrResultMapper ocrResultMapper,
            OcrProcessorResolver ocrProcessorResolver,
            DocumentContentUtil documentContentUtil,
            ConfidenceUtil confidenceUtil,
            OcrProperties ocrProperties) {

        this.ocrResultRepository =
                ocrResultRepository;

        this.ocrResultMapper =
                ocrResultMapper;

        this.ocrProcessorResolver =
                ocrProcessorResolver;

        this.documentContentUtil =
                documentContentUtil;

        this.confidenceUtil =
                confidenceUtil;

        this.ocrProperties =
                ocrProperties;
    }

    @Override
    @Transactional
    public OcrResultResponse processDocument(
            OcrDocumentContent documentContent,
            String language,
            String requestedBy,
            boolean forceReprocess) {

        validateDocumentContent(
                documentContent
        );

        String normalizedUsername =
                normalizeUsername(requestedBy);

        String normalizedLanguage =
                normalizeLanguage(language);

        Long documentId =
                documentContent.getDocumentId();

        preventConcurrentProcessing(
                documentId
        );

        Optional<OcrResult> latestResult =
                ocrResultRepository
                        .findFirstByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
                                documentId
                        );

        if (!forceReprocess
                && latestResult.isPresent()) {

            OcrResult existingResult =
                    latestResult.get();

            if (existingResult.getStatus()
                    == OcrStatus.COMPLETED
                    || existingResult.getStatus()
                    == OcrStatus.LOW_CONFIDENCE) {

                return ocrResultMapper.toResponse(
                        existingResult
                );
            }

            if (existingResult.getStatus()
                    == OcrStatus.FAILED) {

                throw new InvalidOcrRequestException(
                        "The latest OCR attempt failed. "
                                + "Use the retry endpoint or "
                                + "set forceReprocess to true"
                );
            }
        }

        OcrResult ocrResult =
                createInitialResult(
                        documentContent,
                        normalizedLanguage,
                        normalizedUsername
                );

        ocrResult =
                ocrResultRepository.save(
                        ocrResult
                );

        return executeOcr(
                ocrResult,
                documentContent,
                normalizedLanguage
        );
    }

    @Override
    @Transactional
    public OcrResultResponse retryOcr(
            Long ocrResultId,
            OcrDocumentContent documentContent,
            String language,
            String requestedBy,
            String retryReason) {

        validatePositiveId(
                ocrResultId,
                "OCR result ID"
        );

        validateDocumentContent(
                documentContent
        );

        validateRetryReason(
                retryReason
        );

        OcrResult result =
                findActiveResult(
                        ocrResultId
                );

        if (!result.getDocumentId().equals(
                documentContent.getDocumentId())) {

            throw new InvalidOcrRequestException(
                    "Downloaded document ID does not "
                            + "match the OCR result"
            );
        }

        if (result.getStatus()
                != OcrStatus.FAILED
                && result.getStatus()
                != OcrStatus.LOW_CONFIDENCE) {

            throw new InvalidOcrRequestException(
                    "Only FAILED or LOW_CONFIDENCE "
                            + "OCR results can be retried"
            );
        }

        preventConcurrentProcessing(
                result.getDocumentId()
        );

        String normalizedLanguage =
                normalizeLanguage(language);

        String normalizedUsername =
                normalizeUsername(requestedBy);

        result.setOriginalFileName(
                documentContent.getOriginalFileName()
        );

        result.setContentType(
                documentContentUtil
                        .normalizeContentType(
                                documentContent
                                        .getContentType()
                        )
        );

        result.setLanguage(
                normalizedLanguage
        );

        result.setRequestedBy(
                normalizedUsername
        );

        result.setStatus(
                OcrStatus.RETRY_PENDING
        );

        result.setRetryCount(
                result.getRetryCount() + 1
        );

        result.setExtractedText(null);
        result.setAverageConfidence(null);
        result.setPageCount(null);
        result.setCharacterCount(null);
        result.setWordCount(null);
        result.setProcessingDurationMs(null);
        result.setStartedAt(null);
        result.setCompletedAt(null);
        result.setErrorMessage(null);

        result =
                ocrResultRepository.save(
                        result
                );

        return executeOcr(
                result,
                documentContent,
                normalizedLanguage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OcrResultResponse getResultById(
            Long ocrResultId) {

        return ocrResultMapper.toResponse(
                findActiveResult(ocrResultId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OcrResultResponse
    getLatestResultByDocumentId(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        OcrResult result =
                ocrResultRepository
                        .findFirstByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
                                documentId
                        )
                        .orElseThrow(() ->
                                OcrResultNotFoundException
                                        .forDocument(
                                                documentId
                                        )
                        );

        return ocrResultMapper.toResponse(
                result
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrSummaryResponse>
    getHistoryByDocumentId(
            Long documentId) {

        validatePositiveId(
                documentId,
                "Document ID"
        );

        List<OcrResult> results =
                ocrResultRepository
                        .findAllByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
                                documentId
                        );

        return results.stream()
                .map(
                        ocrResultMapper
                                ::toSummaryResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrSummaryResponse>
    getResultsByRequestedUser(
            String requestedBy) {

        String normalizedUsername =
                normalizeUsername(requestedBy);

        return ocrResultRepository
                .findAllByRequestedByAndActiveTrueOrderByCreatedAtDesc(
                        normalizedUsername
                )
                .stream()
                .map(
                        ocrResultMapper
                                ::toSummaryResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrSummaryResponse>
    getResultsByStatus(
            OcrStatus status) {

        if (status == null) {
            throw new InvalidOcrRequestException(
                    "OCR status is required"
            );
        }

        return ocrResultRepository
                .findAllByStatusAndActiveTrueOrderByCreatedAtDesc(
                        status
                )
                .stream()
                .map(
                        ocrResultMapper
                                ::toSummaryResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrSummaryResponse>
    getAllResults() {

        return ocrResultRepository
                .findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(
                        ocrResultMapper
                                ::toSummaryResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusSummary() {

        Map<String, Long> summary =
                new java.util.LinkedHashMap<>();

        summary.put(
                "TOTAL",
                ocrResultRepository.countByActiveTrue()
        );

        for (OcrStatus status
                : OcrStatus.values()) {

            summary.put(
                    status.name(),
                    ocrResultRepository
                            .countByStatusAndActiveTrue(
                                    status
                            )
            );
        }

        return summary;
    }

    private OcrResultResponse executeOcr(
            OcrResult ocrResult,
            OcrDocumentContent documentContent,
            String language) {

        long startTime =
                System.currentTimeMillis();

        ocrResult.setStatus(
                OcrStatus.PROCESSING
        );

        ocrResult.setStartedAt(
                LocalDateTime.now()
        );

        ocrResult.setCompletedAt(null);
        ocrResult.setErrorMessage(null);

        ocrResult =
                ocrResultRepository.save(
                        ocrResult
                );

        try {
            String contentType =
                    documentContentUtil
                            .normalizeContentType(
                                    documentContent
                                            .getContentType()
                            );

            OcrProcessor processor =
                    ocrProcessorResolver.resolve(
                            contentType
                    );

            OcrProcessingResult
                    processingResult =
                    processor.process(
                            documentContent.getContent(),
                            documentContent
                                    .getOriginalFileName(),
                            language
                    );

            applySuccessfulResult(
                    ocrResult,
                    processingResult
            );

            OcrResult savedResult =
                    ocrResultRepository.save(
                            ocrResult
                    );

            return ocrResultMapper.toResponse(
                    savedResult
            );

        } catch (RuntimeException exception) {

            applyFailedResult(
                    ocrResult,
                    exception,
                    startTime
            );

            ocrResultRepository.save(
                    ocrResult
            );

            if (exception
                    instanceof InvalidOcrRequestException) {

                throw exception;
            }

            if (exception
                    instanceof OcrProcessingException) {

                throw exception;
            }

            throw new OcrProcessingException(
                    ocrResult.getDocumentId(),
                    safeErrorMessage(exception),
                    exception
            );
        }
    }

    private void applySuccessfulResult(
            OcrResult ocrResult,
            OcrProcessingResult result) {

        if (result == null) {
            throw new OcrProcessingException(
                    "OCR processor returned no result"
            );
        }

        String extractedText =
                result.getExtractedText();

        if (extractedText == null
                || extractedText.isBlank()) {

            throw new OcrProcessingException(
                    "OCR processing completed but "
                            + "no text was extracted"
            );
        }

        double confidence =
                result.getAverageConfidence();

        OcrStatus completedStatus =
                confidenceUtil.isLowConfidence(
                        confidence,
                        ocrProperties
                                .getMinimumConfidence()
                )
                        ? OcrStatus.LOW_CONFIDENCE
                        : OcrStatus.COMPLETED;

        ocrResult.setExtractedText(
                extractedText
        );

        ocrResult.setAverageConfidence(
                confidence
        );

        ocrResult.setPageCount(
                result.getPageCount()
        );

        ocrResult.setCharacterCount(
                result.getCharacterCount()
        );

        ocrResult.setWordCount(
                result.getWordCount()
        );

        ocrResult.setProcessingDurationMs(
                result.getProcessingDurationMs()
        );

        ocrResult.setStatus(
                completedStatus
        );

        ocrResult.setCompletedAt(
                LocalDateTime.now()
        );

        ocrResult.setErrorMessage(null);
    }

    private void applyFailedResult(
            OcrResult ocrResult,
            RuntimeException exception,
            long startTime) {

        ocrResult.setStatus(
                OcrStatus.FAILED
        );

        ocrResult.setCompletedAt(
                LocalDateTime.now()
        );

        ocrResult.setProcessingDurationMs(
                System.currentTimeMillis()
                        - startTime
        );

        ocrResult.setErrorMessage(
                safeErrorMessage(exception)
        );
    }

    private OcrResult createInitialResult(
            OcrDocumentContent document,
            String language,
            String requestedBy) {

        OcrResult result =
                new OcrResult();

        result.setDocumentId(
                document.getDocumentId()
        );

        result.setOriginalFileName(
                document.getOriginalFileName()
        );

        result.setContentType(
                documentContentUtil
                        .normalizeContentType(
                                document.getContentType()
                        )
        );

        result.setEngine(
                OcrEngine.TESSERACT
        );

        result.setStatus(
                OcrStatus.PENDING
        );

        result.setLanguage(language);

        result.setRequestedBy(
                requestedBy
        );

        result.setRetryCount(0);
        result.setActive(true);

        return result;
    }

    private OcrResult findActiveResult(
            Long ocrResultId) {

        validatePositiveId(
                ocrResultId,
                "OCR result ID"
        );

        return ocrResultRepository
                .findByIdAndActiveTrue(
                        ocrResultId
                )
                .orElseThrow(() ->
                        new OcrResultNotFoundException(
                                ocrResultId
                        )
                );
    }

    private void preventConcurrentProcessing(
            Long documentId) {

        boolean processing =
                ocrResultRepository
                        .existsByDocumentIdAndStatusInAndActiveTrue(
                                documentId,
                                ACTIVE_PROCESSING_STATUSES
                        );

        if (processing) {
            throw new InvalidOcrRequestException(
                    "OCR processing is already active "
                            + "for document ID: "
                            + documentId
            );
        }
    }

    private void validateDocumentContent(
            OcrDocumentContent documentContent) {

        if (documentContent == null) {
            throw new InvalidOcrRequestException(
                    "Document content is required"
            );
        }

        validatePositiveId(
                documentContent.getDocumentId(),
                "Document ID"
        );

        documentContentUtil.validateContent(
                documentContent.getContent(),
                documentContent.getContentType(),
                documentContent.getOriginalFileName()
        );
    }

    private void validatePositiveId(
            Long value,
            String fieldName) {

        if (value == null || value <= 0) {
            throw new InvalidOcrRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }

    private void validateRetryReason(
            String retryReason) {

        if (retryReason != null
                && retryReason.length() > 500) {

            throw new InvalidOcrRequestException(
                    "Retry reason cannot exceed "
                            + "500 characters"
            );
        }
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "system";
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguage(
            String language) {

        String normalizedLanguage;

        if (language == null
                || language.isBlank()) {

            normalizedLanguage =
                    ocrProperties.getLanguage();

        } else {
            normalizedLanguage =
                    language.trim()
                            .toLowerCase(Locale.ROOT);
        }

        if (normalizedLanguage == null
                || normalizedLanguage.isBlank()) {

            throw new InvalidOcrRequestException(
                    "OCR language is required"
            );
        }

        if (!normalizedLanguage.matches(
                "[a-z0-9_+\\-]{2,30}")) {

            throw new InvalidOcrRequestException(
                    "Invalid OCR language code: "
                            + normalizedLanguage
            );
        }

        return normalizedLanguage;
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

        if (message.length() > 2000) {
            return message.substring(
                    0,
                    2000
            );
        }

        return message;
    }
}