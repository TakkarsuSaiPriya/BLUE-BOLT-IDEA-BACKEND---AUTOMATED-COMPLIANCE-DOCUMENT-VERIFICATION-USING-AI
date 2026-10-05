package com.compliance.verificationservice.audit;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.enums.ReviewDecision;
import com.compliance.verificationservice.enums.VerificationAuditAction;
import com.compliance.verificationservice.filter.CorrelationIdFilter;
import com.compliance.verificationservice.service.interfaces.VerificationAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class VerificationAuditAspect {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    VerificationAuditAspect.class
            );

    private final VerificationAuditService
            verificationAuditService;

    public VerificationAuditAspect(
            VerificationAuditService
                    verificationAuditService) {

        this.verificationAuditService =
                verificationAuditService;
    }

    @Around(
            "execution(* com.compliance.verificationservice.service.impl.VerificationServiceImpl.*(..))"
    )
    public Object auditVerificationOperation(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        String methodName =
                joinPoint.getSignature()
                        .getName();

        Object[] arguments =
                joinPoint.getArgs();

        AuditMetadata metadata =
                buildInitialMetadata(
                        methodName,
                        arguments
                );

        long startedAt =
                System.nanoTime();

        try {

            Object result =
                    joinPoint.proceed();

            updateMetadataFromResult(
                    metadata,
                    result
            );

            metadata.action =
                    resolveSuccessAction(
                            methodName,
                            result
                    );

            safelyRecordAudit(
                    metadata,
                    "SUCCESS",
                    buildSuccessDetails(
                            methodName,
                            result
                    ),
                    calculateDurationMs(
                            startedAt
                    )
            );

            return result;

        } catch (Throwable throwable) {

            metadata.action =
                    resolveFailureAction(
                            methodName
                    );

            safelyRecordAudit(
                    metadata,
                    "FAILED",
                    buildFailureDetails(
                            methodName,
                            throwable
                    ),
                    calculateDurationMs(
                            startedAt
                    )
            );

            throw throwable;
        }
    }

    private AuditMetadata buildInitialMetadata(
            String methodName,
            Object[] arguments) {

        AuditMetadata metadata =
                new AuditMetadata();

        metadata.username =
                resolveUsername();

        metadata.correlationId =
                resolveCorrelationId();

        metadata.clientIp =
                resolveClientIp();

        metadata.requestPath =
                resolveRequestPath();

        if (arguments == null) {
            return metadata;
        }

        for (Object argument : arguments) {

            if (argument instanceof OcrResultInternalResponse ocrResult) {

                metadata.ocrResultId =
                        ocrResult.getId();

                metadata.documentId =
                        ocrResult.getDocumentId();
            }

            if (argument instanceof ReviewDecisionRequest request) {

                metadata.reviewDecision =
                        request.getDecision();
            }
        }

        if (arguments.length > 0
                && arguments[0] instanceof Long id) {

            if ("getLatestByDocumentId".equals(
                    methodName)
                    || "getDocumentHistory".equals(
                    methodName)) {

                metadata.documentId = id;

            } else {

                metadata.verificationResultId = id;
            }
        }

        return metadata;
    }

    private void updateMetadataFromResult(
            AuditMetadata metadata,
            Object result) {

        if (!(result
                instanceof VerificationResultResponse response)) {

            return;
        }

        metadata.verificationResultId =
                response.getId();

        metadata.documentId =
                response.getDocumentId();

        metadata.ocrResultId =
                response.getOcrResultId();

        metadata.username =
                response.getRequestedBy() == null
                        ? metadata.username
                        : response.getRequestedBy();

        metadata.reviewDecision =
                response.getReviewDecision();
    }

    private VerificationAuditAction resolveSuccessAction(
            String methodName,
            Object result) {

        return switch (methodName) {

            case "processVerification" ->
                    VerificationAuditAction
                            .VERIFICATION_COMPLETED;

            case "retryVerification" ->
                    VerificationAuditAction
                            .VERIFICATION_RETRY_COMPLETED;

            case "reviewVerification" ->
                    resolveReviewAction(
                            result
                    );

            case "getVerificationResult" ->
                    VerificationAuditAction
                            .VERIFICATION_RESULT_VIEWED;

            case "getLatestByDocumentId" ->
                    VerificationAuditAction
                            .VERIFICATION_LATEST_RESULT_VIEWED;

            case "getDocumentHistory" ->
                    VerificationAuditAction
                            .VERIFICATION_HISTORY_VIEWED;

            case "getResultsForUser" ->
                    VerificationAuditAction
                            .VERIFICATION_USER_RESULTS_VIEWED;

            case "getResultsByStatus" ->
                    VerificationAuditAction
                            .VERIFICATION_RESULTS_FILTERED;

            case "getAllResults" ->
                    VerificationAuditAction
                            .VERIFICATION_RESULTS_LISTED;

            case "getStatusSummary" ->
                    VerificationAuditAction
                            .VERIFICATION_SUMMARY_VIEWED;

            default ->
                    VerificationAuditAction
                            .VERIFICATION_RESULT_VIEWED;
        };
    }

    private VerificationAuditAction resolveFailureAction(
            String methodName) {

        return switch (methodName) {

            case "processVerification" ->
                    VerificationAuditAction
                            .VERIFICATION_FAILED;

            case "retryVerification" ->
                    VerificationAuditAction
                            .VERIFICATION_RETRY_FAILED;

            case "reviewVerification" ->
                    VerificationAuditAction
                            .MANUAL_REVIEW_REQUESTED;

            default ->
                    VerificationAuditAction
                            .VERIFICATION_RESULT_VIEWED;
        };
    }

    private VerificationAuditAction resolveReviewAction(
            Object result) {

        if (!(result
                instanceof VerificationResultResponse response)
                || response.getReviewDecision() == null) {

            return VerificationAuditAction
                    .MANUAL_REVIEW_REQUESTED;
        }

        return switch (
                response.getReviewDecision()) {

            case APPROVED ->
                    VerificationAuditAction
                            .MANUAL_REVIEW_APPROVED;

            case REJECTED ->
                    VerificationAuditAction
                            .MANUAL_REVIEW_REJECTED;

            case RETURNED_FOR_REPROCESSING ->
                    VerificationAuditAction
                            .MANUAL_REVIEW_RETURNED_FOR_REPROCESSING;
        };
    }

    private String buildSuccessDetails(
            String methodName,
            Object result) {

        if (result
                instanceof VerificationResultResponse response) {

            return limitText(
                    "Operation "
                            + methodName
                            + " completed with verification status "
                            + response.getStatus()
                            + " and score "
                            + response.getVerificationScore(),
                    2000
            );
        }

        return limitText(
                "Operation "
                        + methodName
                        + " completed successfully",
                2000
        );
    }

    private String buildFailureDetails(
            String methodName,
            Throwable throwable) {

        String failureMessage =
                throwable == null
                        || throwable.getMessage() == null
                        || throwable.getMessage().isBlank()
                        ? "Unknown failure"
                        : throwable.getMessage().trim();

        return limitText(
                "Operation "
                        + methodName
                        + " failed: "
                        + failureMessage,
                2000
        );
    }

    private void safelyRecordAudit(
            AuditMetadata metadata,
            String executionStatus,
            String details,
            Long processingDurationMs) {

        try {

            verificationAuditService.recordAudit(
                    metadata.verificationResultId,
                    metadata.documentId,
                    metadata.ocrResultId,
                    metadata.action,
                    executionStatus,
                    metadata.username,
                    details,
                    metadata.correlationId,
                    metadata.clientIp,
                    metadata.requestPath,
                    processingDurationMs
            );

        } catch (Exception exception) {

            LOGGER.error(
                    "Unable to persist verification audit record. "
                            + "action={}, correlationId={}",
                    metadata.action,
                    metadata.correlationId,
                    exception
            );
        }
    }

    private String resolveUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            return "system";
        }

        return limitText(
                authentication.getName(),
                150
        );
    }

    private String resolveCorrelationId() {

        String correlationId =
                MDC.get(
                        CorrelationIdFilter.MDC_KEY
                );

        if (correlationId == null
                || correlationId.isBlank()) {

            return UUID.randomUUID()
                    .toString();
        }

        return limitText(
                correlationId,
                100
        );
    }

    private String resolveClientIp() {

        HttpServletRequest request =
                getCurrentRequest();

        if (request == null) {
            return null;
        }

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            String firstAddress =
                    forwardedFor.split(
                            ","
                    )[0];

            return limitText(
                    firstAddress.trim(),
                    64
            );
        }

        return limitText(
                request.getRemoteAddr(),
                64
        );
    }

    private String resolveRequestPath() {

        HttpServletRequest request =
                getCurrentRequest();

        if (request == null) {
            return "internal-processing";
        }

        return limitText(
                request.getRequestURI(),
                500
        );
    }

    private HttpServletRequest getCurrentRequest() {

        if (!(RequestContextHolder
                .getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) {

            return null;
        }

        return attributes.getRequest();
    }

    private long calculateDurationMs(
            long startedAt) {

        return Math.max(
                (System.nanoTime() - startedAt)
                        / 1_000_000L,
                0L
        );
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        String normalized =
                value.trim();

        if (normalized.length() > maximumLength) {
            return normalized.substring(
                    0,
                    maximumLength
            );
        }

        return normalized;
    }

    private static class AuditMetadata {

        private Long verificationResultId;
        private Long documentId;
        private Long ocrResultId;
        private VerificationAuditAction action;
        private String username;
        private String correlationId;
        private String clientIp;
        private String requestPath;
        private ReviewDecision reviewDecision;
    }
}