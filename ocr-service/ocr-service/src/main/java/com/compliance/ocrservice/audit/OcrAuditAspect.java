package com.compliance.ocrservice.audit;

import com.compliance.ocrservice.dto.request.ManualOcrRequest;
import com.compliance.ocrservice.dto.request.OcrRetryRequest;
import com.compliance.ocrservice.dto.response.ApiResponse;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.enums.OcrAuditAction;
import com.compliance.ocrservice.enums.OcrStatus;
import com.compliance.ocrservice.event.DocumentUploadedEvent;
import com.compliance.ocrservice.service.interfaces.OcrAuditService;
import com.compliance.ocrservice.util.SecurityContextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Aspect
@Component
public class OcrAuditAspect {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OcrAuditAspect.class
            );

    private static final String SUCCESS =
            "SUCCESS";

    private static final String FAILED =
            "FAILED";

    private static final String SYSTEM =
            "system";

    private static final int MAXIMUM_PAYLOAD_LENGTH =
            10000;

    private static final int MAXIMUM_ERROR_LENGTH =
            2000;

    private final OcrAuditService
            ocrAuditService;

    private final SecurityContextUtil
            securityContextUtil;

    private final ObjectMapper
            objectMapper;

    public OcrAuditAspect(
            OcrAuditService ocrAuditService,
            SecurityContextUtil securityContextUtil,
            ObjectMapper objectMapper) {

        this.ocrAuditService =
                ocrAuditService;

        this.securityContextUtil =
                securityContextUtil;

        this.objectMapper =
                objectMapper;
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrCommandController.processDocument(..))"
    )
    public Object auditManualProcessing(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        ManualOcrRequest request =
                getArgument(
                        joinPoint,
                        ManualOcrRequest.class
                );

        Long documentId =
                request == null
                        ? null
                        : request.getDocumentId();

        String username =
                resolveUsername();

        String requestPayload =
                createManualRequestPayload(
                        request
                );

        recordSafely(
                OcrAuditAction.OCR_PROCESS_REQUESTED,
                documentId,
                null,
                username,
                SUCCESS,
                requestPayload,
                null,
                null
        );

        try {
            Object response =
                    joinPoint.proceed();

            OcrResultResponse result =
                    extractOcrResult(
                            response
                    );

            recordSafely(
                    OcrAuditAction.OCR_PROCESS_COMPLETED,
                    result == null
                            ? documentId
                            : result.getDocumentId(),
                    result == null
                            ? null
                            : result.getId(),
                    username,
                    SUCCESS,
                    requestPayload,
                    createResultSummary(
                            result
                    ),
                    null
            );

            return response;

        } catch (Throwable throwable) {

            recordSafely(
                    OcrAuditAction.OCR_PROCESS_FAILED,
                    documentId,
                    null,
                    username,
                    FAILED,
                    requestPayload,
                    null,
                    safeErrorMessage(
                            throwable
                    )
            );

            throw throwable;
        }
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrCommandController.retryOcr(..))"
    )
    public Object auditRetry(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Long ocrResultId =
                getLongArgument(
                        joinPoint,
                        0
                );

        OcrRetryRequest request =
                getArgument(
                        joinPoint,
                        OcrRetryRequest.class
                );

        String username =
                resolveUsername();

        String requestPayload =
                createRetryRequestPayload(
                        ocrResultId,
                        request
                );

        recordSafely(
                OcrAuditAction.OCR_RETRY_REQUESTED,
                null,
                ocrResultId,
                username,
                SUCCESS,
                requestPayload,
                null,
                null
        );

        try {
            Object response =
                    joinPoint.proceed();

            OcrResultResponse result =
                    extractOcrResult(
                            response
                    );

            recordSafely(
                    OcrAuditAction.OCR_RETRY_COMPLETED,
                    result == null
                            ? null
                            : result.getDocumentId(),
                    result == null
                            ? ocrResultId
                            : result.getId(),
                    username,
                    SUCCESS,
                    requestPayload,
                    createResultSummary(
                            result
                    ),
                    null
            );

            return response;

        } catch (Throwable throwable) {

            recordSafely(
                    OcrAuditAction.OCR_RETRY_FAILED,
                    null,
                    ocrResultId,
                    username,
                    FAILED,
                    requestPayload,
                    null,
                    safeErrorMessage(
                            throwable
                    )
            );

            throw throwable;
        }
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController.getResultById(..))"
    )
    public Object auditResultViewed(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Long ocrResultId =
                getLongArgument(
                        joinPoint,
                        0
                );

        Map<String, Object> requestData =
                new LinkedHashMap<>();

        requestData.put(
                "ocrResultId",
                nullableValue(ocrResultId)
        );

        return auditQuery(
                joinPoint,
                OcrAuditAction.OCR_RESULT_VIEWED,
                null,
                ocrResultId,
                requestData
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController."
                    + "getLatestResultByDocumentId(..))"
    )
    public Object auditLatestResultViewed(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Long documentId =
                getLongArgument(
                        joinPoint,
                        0
                );

        Map<String, Object> requestData =
                new LinkedHashMap<>();

        requestData.put(
                "documentId",
                nullableValue(documentId)
        );

        return auditQuery(
                joinPoint,
                OcrAuditAction
                        .OCR_LATEST_RESULT_VIEWED,
                documentId,
                null,
                requestData
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController."
                    + "getHistoryByDocumentId(..))"
    )
    public Object auditHistoryViewed(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Long documentId =
                getLongArgument(
                        joinPoint,
                        0
                );

        Map<String, Object> requestData =
                new LinkedHashMap<>();

        requestData.put(
                "documentId",
                nullableValue(documentId)
        );

        return auditQuery(
                joinPoint,
                OcrAuditAction.OCR_HISTORY_VIEWED,
                documentId,
                null,
                requestData
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController."
                    + "getCurrentUserResults(..))"
    )
    public Object auditCurrentUserResults(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        return auditQuery(
                joinPoint,
                OcrAuditAction
                        .OCR_USER_RESULTS_VIEWED,
                null,
                null,
                Map.of(
                        "scope",
                        "CURRENT_USER"
                )
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController."
                    + "getResultsByStatus(..))"
    )
    public Object auditStatusFilter(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        OcrStatus status =
                getArgument(
                        joinPoint,
                        OcrStatus.class
                );

        return auditQuery(
                joinPoint,
                OcrAuditAction.OCR_RESULTS_FILTERED,
                null,
                null,
                Map.of(
                        "status",
                        status == null
                                ? "null"
                                : status.name()
                )
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController.getStatusSummary(..))"
    )
    public Object auditStatusSummary(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        return auditQuery(
                joinPoint,
                OcrAuditAction.OCR_SUMMARY_VIEWED,
                null,
                null,
                Map.of(
                        "scope",
                        "STATUS_SUMMARY"
                )
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.controller."
                    + "OcrQueryController.getAllResults(..))"
    )
    public Object auditAllResults(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        return auditQuery(
                joinPoint,
                OcrAuditAction.OCR_RESULTS_LISTED,
                null,
                null,
                Map.of(
                        "scope",
                        "ALL_ACTIVE_RESULTS"
                )
        );
    }

    @Around(
            "execution(* com.compliance.ocrservice.consumer."
                    + "DocumentUploadedConsumer."
                    + "consumeDocumentUploaded(..))"
    )
    public Object auditDocumentUploadedEvent(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Message message =
                getArgument(
                        joinPoint,
                        Message.class
                );

        DocumentUploadedEvent event =
                extractDocumentUploadedEvent(
                        message
                );

        Long documentId =
                event == null
                        ? null
                        : event.getDocumentId();

        String username =
                event == null
                        ? SYSTEM
                        : normalizeUsername(
                        event.getUploadedBy()
                );

        String eventPayload =
                createEventPayload(
                        event
                );

        recordSafely(
                OcrAuditAction
                        .DOCUMENT_UPLOAD_EVENT_RECEIVED,
                documentId,
                null,
                username,
                SUCCESS,
                eventPayload,
                null,
                null
        );

        try {
            Object result =
                    joinPoint.proceed();

            recordSafely(
                    OcrAuditAction
                            .DOCUMENT_UPLOAD_EVENT_COMPLETED,
                    documentId,
                    null,
                    username,
                    SUCCESS,
                    eventPayload,
                    serializeSafely(
                            Map.of(
                                    "message",
                                    "Automatic OCR processing completed"
                            )
                    ),
                    null
            );

            return result;

        } catch (Throwable throwable) {

            recordSafely(
                    OcrAuditAction
                            .DOCUMENT_UPLOAD_EVENT_FAILED,
                    documentId,
                    null,
                    username,
                    FAILED,
                    eventPayload,
                    null,
                    safeErrorMessage(
                            throwable
                    )
            );

            throw throwable;
        }
    }

    private Object auditQuery(
            ProceedingJoinPoint joinPoint,
            OcrAuditAction action,
            Long documentId,
            Long ocrResultId,
            Map<String, Object> requestData)
            throws Throwable {

        String username =
                resolveUsername();

        String requestPayload =
                serializeSafely(
                        requestData
                );

        try {
            Object response =
                    joinPoint.proceed();

            AuditResponseMetadata metadata =
                    extractResponseMetadata(
                            response
                    );

            Long resolvedDocumentId =
                    metadata.documentId() == null
                            ? documentId
                            : metadata.documentId();

            Long resolvedOcrResultId =
                    metadata.ocrResultId() == null
                            ? ocrResultId
                            : metadata.ocrResultId();

            recordSafely(
                    action,
                    resolvedDocumentId,
                    resolvedOcrResultId,
                    username,
                    SUCCESS,
                    requestPayload,
                    metadata.responsePayload(),
                    null
            );

            return response;

        } catch (Throwable throwable) {

            recordSafely(
                    action,
                    documentId,
                    ocrResultId,
                    username,
                    FAILED,
                    requestPayload,
                    null,
                    safeErrorMessage(
                            throwable
                    )
            );

            throw throwable;
        }
    }

    private OcrResultResponse extractOcrResult(
            Object response) {

        Object body =
                extractResponseBody(
                        response
                );

        if (body
                instanceof ApiResponse<?> apiResponse) {

            Object data =
                    apiResponse.getData();

            if (data
                    instanceof OcrResultResponse result) {

                return result;
            }
        }

        if (body
                instanceof OcrResultResponse result) {

            return result;
        }

        return null;
    }

    private AuditResponseMetadata
    extractResponseMetadata(
            Object response) {

        Object body =
                extractResponseBody(
                        response
                );

        if (body
                instanceof ApiResponse<?> apiResponse) {

            return createMetadata(
                    apiResponse.getData()
            );
        }

        return createMetadata(body);
    }

    private AuditResponseMetadata createMetadata(
            Object data) {

        if (data
                instanceof OcrResultResponse result) {

            return new AuditResponseMetadata(
                    result.getDocumentId(),
                    result.getId(),
                    createResultSummary(
                            result
                    )
            );
        }

        if (data instanceof List<?> list) {

            return new AuditResponseMetadata(
                    null,
                    null,
                    serializeSafely(
                            Map.of(
                                    "resultCount",
                                    list.size()
                            )
                    )
            );
        }

        if (data instanceof Map<?, ?> map) {

            return new AuditResponseMetadata(
                    null,
                    null,
                    serializeSafely(map)
            );
        }

        return new AuditResponseMetadata(
                null,
                null,
                serializeSafely(
                        Map.of(
                                "responseAvailable",
                                data != null
                        )
                )
        );
    }

    private Object extractResponseBody(
            Object response) {

        if (response
                instanceof ResponseEntity<?> responseEntity) {

            return responseEntity.getBody();
        }

        return response;
    }

    private String createManualRequestPayload(
            ManualOcrRequest request) {

        if (request == null) {
            return null;
        }

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "documentId",
                request.getDocumentId()
        );

        payload.put(
                "language",
                request.getLanguage()
        );

        payload.put(
                "forceReprocess",
                request.isForceReprocess()
        );

        return serializeSafely(payload);
    }

    private String createRetryRequestPayload(
            Long ocrResultId,
            OcrRetryRequest request) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "ocrResultId",
                ocrResultId
        );

        if (request != null) {
            payload.put(
                    "language",
                    request.getLanguage()
            );

            payload.put(
                    "reason",
                    limitText(
                            request.getReason(),
                            500
                    )
            );
        }

        return serializeSafely(payload);
    }

    private String createResultSummary(
            OcrResultResponse result) {

        if (result == null) {
            return null;
        }

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "ocrResultId",
                result.getId()
        );

        payload.put(
                "documentId",
                result.getDocumentId()
        );

        payload.put(
                "status",
                result.getStatus() == null
                        ? null
                        : result.getStatus().name()
        );

        payload.put(
                "language",
                result.getLanguage()
        );

        payload.put(
                "averageConfidence",
                result.getAverageConfidence()
        );

        payload.put(
                "pageCount",
                result.getPageCount()
        );

        payload.put(
                "characterCount",
                result.getCharacterCount()
        );

        payload.put(
                "wordCount",
                result.getWordCount()
        );

        payload.put(
                "processingDurationMs",
                result.getProcessingDurationMs()
        );

        payload.put(
                "retryCount",
                result.getRetryCount()
        );

        /*
         * Extracted text is deliberately excluded.
         */
        return serializeSafely(payload);
    }

    private DocumentUploadedEvent
    extractDocumentUploadedEvent(
            Message message) {

        if (!(message
                instanceof TextMessage textMessage)) {

            return null;
        }

        try {
            String json =
                    textMessage.getText();

            if (json == null
                    || json.isBlank()) {

                return null;
            }

            return objectMapper.readValue(
                    json,
                    DocumentUploadedEvent.class
            );

        } catch (JMSException
                 | JsonProcessingException exception) {

            LOGGER.debug(
                    "Unable to extract document-upload "
                            + "event for auditing",
                    exception
            );

            return null;
        }
    }

    private String createEventPayload(
            DocumentUploadedEvent event) {

        if (event == null) {
            return null;
        }

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "eventId",
                event.getEventId()
        );

        payload.put(
                "eventType",
                event.getEventType()
        );

        payload.put(
                "documentId",
                event.getDocumentId()
        );

        payload.put(
                "originalFileName",
                event.getOriginalFileName()
        );

        payload.put(
                "contentType",
                event.getContentType()
        );

        payload.put(
                "documentType",
                event.getDocumentType()
        );

        payload.put(
                "uploadedBy",
                event.getUploadedBy()
        );

        payload.put(
                "occurredAt",
                event.getOccurredAt()
        );

        return serializeSafely(payload);
    }

    private String resolveUsername() {

        try {
            return securityContextUtil
                    .getCurrentUsername();

        } catch (RuntimeException exception) {

            return SYSTEM;
        }
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return SYSTEM;
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    private void recordSafely(
            OcrAuditAction action,
            Long documentId,
            Long ocrResultId,
            String username,
            String executionStatus,
            String requestPayload,
            String responsePayload,
            String errorMessage) {

        try {
            ocrAuditService.recordAudit(
                    action,
                    documentId,
                    ocrResultId,
                    username,
                    executionStatus,
                    requestPayload,
                    responsePayload,
                    errorMessage
            );

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "OCR audit operation failed. action={}",
                    action,
                    exception
            );
        }
    }

    private String serializeSafely(
            Object value) {

        if (value == null) {
            return null;
        }

        try {
            String json =
                    objectMapper.writeValueAsString(
                            value
                    );

            return limitText(
                    json,
                    MAXIMUM_PAYLOAD_LENGTH
            );

        } catch (JsonProcessingException exception) {

            LOGGER.debug(
                    "Unable to serialize OCR audit payload",
                    exception
            );

            return "{\"serializationError\":true}";
        }
    }

    private String safeErrorMessage(
            Throwable throwable) {

        if (throwable == null
                || throwable.getMessage() == null
                || throwable.getMessage().isBlank()) {

            return "OCR operation failed";
        }

        return limitText(
                throwable.getMessage().trim(),
                MAXIMUM_ERROR_LENGTH
        );
    }

    private String limitText(
            String text,
            int maximumLength) {

        if (text == null) {
            return null;
        }

        if (maximumLength <= 0) {
            return "";
        }

        if (text.length() > maximumLength) {
            return text.substring(
                    0,
                    maximumLength
            );
        }

        return text;
    }

    private Long getLongArgument(
            ProceedingJoinPoint joinPoint,
            int argumentIndex) {

        Object[] arguments =
                joinPoint.getArgs();

        if (arguments == null
                || argumentIndex < 0
                || argumentIndex >= arguments.length) {

            return null;
        }

        Object argument =
                arguments[argumentIndex];

        if (argument instanceof Long value) {
            return value;
        }

        if (argument instanceof Number value) {
            return value.longValue();
        }

        return null;
    }

    private <T> T getArgument(
            ProceedingJoinPoint joinPoint,
            Class<T> argumentType) {

        Object[] arguments =
                joinPoint.getArgs();

        if (arguments == null
                || argumentType == null) {

            return null;
        }

        for (Object argument : arguments) {

            if (argumentType.isInstance(
                    argument)) {

                return argumentType.cast(
                        argument
                );
            }
        }

        return null;
    }

    private Object nullableValue(
            Object value) {

        return value == null
                ? "null"
                : value;
    }

    private record AuditResponseMetadata(
            Long documentId,
            Long ocrResultId,
            String responsePayload) {
    }
}