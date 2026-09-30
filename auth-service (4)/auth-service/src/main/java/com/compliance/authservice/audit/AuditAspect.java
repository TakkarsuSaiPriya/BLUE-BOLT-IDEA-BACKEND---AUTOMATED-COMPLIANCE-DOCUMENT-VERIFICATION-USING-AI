package com.compliance.authservice.audit;

import com.compliance.authservice.dto.request.LoginRequest;
import com.compliance.authservice.dto.request.RefreshTokenRequest;
import com.compliance.authservice.dto.request.RegisterRequest;
import com.compliance.authservice.entity.AuditLog;
import com.compliance.authservice.enums.AuditAction;
import com.compliance.authservice.service.interfaces.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditAspect(
            AuditService auditService,
            ObjectMapper objectMapper) {

        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object audit(
            ProceedingJoinPoint joinPoint,
            Auditable auditable) throws Throwable {

        AuditAction action = auditable.action();
        String requestPayload = serializeRequest(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();

            saveAudit(
                    action,
                    getCurrentUsername(),
                    requestPayload,
                    "Request completed successfully",
                    "SUCCESS"
            );

            return result;

        } catch (Throwable throwable) {

            saveAudit(
                    action,
                    getCurrentUsername(),
                    requestPayload,
                    safeErrorMessage(throwable),
                    "FAILED"
            );

            throw throwable;
        }
    }

    private String serializeRequest(Object[] arguments) {

        try {
            Object[] safeArguments = new Object[arguments.length];

            for (int index = 0; index < arguments.length; index++) {
                safeArguments[index] =
                        sanitizeArgument(arguments[index]);
            }

            return objectMapper.writeValueAsString(safeArguments);

        } catch (Exception exception) {
            return "Unable to serialize request";
        }
    }

    private Object sanitizeArgument(Object argument) {

        if (argument instanceof RegisterRequest request) {

            Map<String, Object> safeRequest = new LinkedHashMap<>();

            safeRequest.put("firstName", request.getFirstName());
            safeRequest.put("lastName", request.getLastName());
            safeRequest.put("username", request.getUsername());
            safeRequest.put("email", request.getEmail());
            safeRequest.put("password", "[REDACTED]");

            return safeRequest;
        }

        if (argument instanceof LoginRequest request) {

            Map<String, Object> safeRequest = new LinkedHashMap<>();

            safeRequest.put("username", request.getUsername());
            safeRequest.put("password", "[REDACTED]");

            return safeRequest;
        }

        if (argument instanceof RefreshTokenRequest) {

            Map<String, Object> safeRequest = new LinkedHashMap<>();

            safeRequest.put("refreshToken", "[REDACTED]");

            return safeRequest;
        }

        return argument;
    }

    private void saveAudit(
            AuditAction action,
            String username,
            String requestPayload,
            String responsePayload,
            String status) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setRequestPayload(requestPayload);
        auditLog.setResponsePayload(responsePayload);
        auditLog.setStatus(status);
        auditLog.setTimestamp(LocalDateTime.now());

        auditService.save(auditLog);
    }

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return "SYSTEM";
        }

        return authentication.getName();
    }

    private String safeErrorMessage(Throwable throwable) {

        if (throwable.getMessage() == null
                || throwable.getMessage().isBlank()) {

            return "Request failed";
        }

        return throwable.getMessage();
    }
}