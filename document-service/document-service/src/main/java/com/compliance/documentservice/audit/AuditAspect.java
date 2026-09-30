package com.compliance.documentservice.audit;

import com.compliance.documentservice.dto.response.ApiResponse;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.enums.AuditActionType;
import com.compliance.documentservice.mapper.DocumentAuditMapper;
import com.compliance.documentservice.producer.DocumentAuditEventProducer;
import com.compliance.documentservice.service.interfaces.DocumentAuditService;
import com.compliance.documentservice.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuditAspect.class);

    private final DocumentAuditService documentAuditService;
    private final DocumentAuditMapper documentAuditMapper;
    private final DocumentAuditEventProducer auditEventProducer;
    private final AuditPayloadSanitizer payloadSanitizer;
    private final SecurityContextUtil securityContextUtil;

    public AuditAspect(
            DocumentAuditService documentAuditService,
            DocumentAuditMapper documentAuditMapper,
            DocumentAuditEventProducer auditEventProducer,
            AuditPayloadSanitizer payloadSanitizer,
            SecurityContextUtil securityContextUtil) {

        this.documentAuditService =
                documentAuditService;

        this.documentAuditMapper =
                documentAuditMapper;

        this.auditEventProducer =
                auditEventProducer;

        this.payloadSanitizer =
                payloadSanitizer;

        this.securityContextUtil =
                securityContextUtil;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(
            ProceedingJoinPoint joinPoint,
            Auditable auditable) throws Throwable {

        HttpServletRequest request =
                getCurrentRequest();

        AuditActionType action =
                auditable.action();

        String username =
                resolveUsername();

        String requestMethod =
                request == null
                        ? null
                        : request.getMethod();

        String requestPath =
                request == null
                        ? null
                        : request.getRequestURI();

        String ipAddress =
                resolveIpAddress(request);

        String requestPayload =
                payloadSanitizer.sanitizeRequest(
                        joinPoint.getArgs()
                );

        try {
            Object result =
                    joinPoint.proceed();

            String responsePayload =
                    payloadSanitizer.sanitizeResponse(
                            result
                    );

            Long documentId =
                    extractDocumentId(
                            joinPoint.getArgs(),
                            result
                    );

            saveAuditSafely(
                    action,
                    username,
                    documentId,
                    requestMethod,
                    requestPath,
                    requestPayload,
                    responsePayload,
                    "SUCCESS",
                    ipAddress,
                    null
            );

            return result;

        } catch (Throwable throwable) {

            Long documentId =
                    extractDocumentId(
                            joinPoint.getArgs(),
                            null
                    );

            saveAuditSafely(
                    action,
                    username,
                    documentId,
                    requestMethod,
                    requestPath,
                    requestPayload,
                    "{\"message\":\"Request failed\"}",
                    "FAILED",
                    ipAddress,
                    safeErrorMessage(throwable)
            );

            throw throwable;
        }
    }

    private void saveAuditSafely(
            AuditActionType action,
            String username,
            Long documentId,
            String requestMethod,
            String requestPath,
            String requestPayload,
            String responsePayload,
            String executionStatus,
            String ipAddress,
            String errorMessage) {

        try {
            DocumentAuditLog auditLog =
                    documentAuditMapper.createAuditLog(
                            action,
                            username,
                            documentId,
                            requestMethod,
                            requestPath,
                            requestPayload,
                            responsePayload,
                            executionStatus,
                            ipAddress,
                            errorMessage
                    );

            DocumentAuditLog savedAuditLog =
                    documentAuditService.saveAuditLog(
                            auditLog
                    );

            try {
                auditEventProducer.publishAuditEvent(
                        savedAuditLog
                );

            } catch (RuntimeException exception) {

                LOGGER.error(
                        "Audit record was saved, but centralized audit event publication failed",
                        exception
                );
            }

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to persist document audit record",
                    exception
            );
        }
    }

    private Long extractDocumentId(
            Object[] arguments,
            Object result) {

        Long resultDocumentId =
                extractDocumentIdFromResult(result);

        if (resultDocumentId != null) {
            return resultDocumentId;
        }

        if (arguments == null) {
            return null;
        }

        for (Object argument : arguments) {

            if (argument instanceof Long value
                    && value > 0) {

                return value;
            }
        }

        return null;
    }

    private Long extractDocumentIdFromResult(
            Object result) {

        if (result instanceof DocumentUploadResponse response) {
            return response.getDocumentId();
        }

        if (result instanceof DocumentResponse response) {
            return response.getId();
        }

        if (result instanceof ApiResponse<?> apiResponse) {

            Object data =
                    apiResponse.getData();

            if (data instanceof DocumentUploadResponse uploadResponse) {
                return uploadResponse.getDocumentId();
            }

            if (data instanceof DocumentResponse documentResponse) {
                return documentResponse.getId();
            }
        }

        if (result instanceof org.springframework.http.ResponseEntity<?> entity) {

            return extractDocumentIdFromResult(
                    entity.getBody()
            );
        }

        return null;
    }

    private String resolveUsername() {

        try {
            return securityContextUtil.getCurrentUsername();

        } catch (RuntimeException exception) {
            return "SYSTEM";
        }
    }

    private HttpServletRequest getCurrentRequest() {

        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {

            return attributes.getRequest();
        }

        return null;
    }

    private String resolveIpAddress(
            HttpServletRequest request) {

        if (request == null) {
            return null;
        }

        String forwardedAddress =
                request.getHeader("X-Forwarded-For");

        if (forwardedAddress != null
                && !forwardedAddress.isBlank()) {

            int commaPosition =
                    forwardedAddress.indexOf(',');

            if (commaPosition > 0) {
                return forwardedAddress
                        .substring(0, commaPosition)
                        .trim();
            }

            return forwardedAddress.trim();
        }

        return request.getRemoteAddr();
    }

    private String safeErrorMessage(
            Throwable throwable) {

        if (throwable == null
                || throwable.getMessage() == null
                || throwable.getMessage().isBlank()) {

            return "Request processing failed";
        }

        String message =
                throwable.getMessage();

        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }

        return message;
    }
}