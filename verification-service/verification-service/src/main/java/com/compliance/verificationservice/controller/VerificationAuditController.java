package com.compliance.verificationservice.controller;

import com.compliance.verificationservice.dto.response.ApiResponse;
import com.compliance.verificationservice.dto.response.VerificationAuditLogResponse;
import com.compliance.verificationservice.enums.VerificationAuditAction;
import com.compliance.verificationservice.service.interfaces.VerificationAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/verification-audit-logs")
@Validated
@Tag(
        name = "Verification Audit",
        description = "Verification audit-log queries"
)
public class VerificationAuditController {

    private final VerificationAuditService
            verificationAuditService;

    public VerificationAuditController(
            VerificationAuditService
                    verificationAuditService) {

        this.verificationAuditService =
                verificationAuditService;
    }

    @GetMapping
    @Operation(
            summary = "List verification audit logs"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getAuditLogs(
            @RequestParam(
                    value = "action",
                    required = false
            )
            VerificationAuditAction action) {

        List<VerificationAuditLogResponse> results =
                action == null
                        ? verificationAuditService
                        .getAllAuditLogs()
                        : verificationAuditService
                        .getAuditLogsByAction(
                                action
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification audit logs retrieved successfully",
                        results
                )
        );
    }

    @GetMapping("/verifications/{verificationResultId}")
    @Operation(
            summary = "Get audit logs for a verification result"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getByVerificationResultId(
            @PathVariable
            @Positive(
                    message =
                            "Verification result ID must be positive"
            )
            Long verificationResultId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification-result audit logs retrieved successfully",
                        verificationAuditService
                                .getAuditLogsByVerificationResultId(
                                        verificationResultId
                                )
                )
        );
    }

    @GetMapping("/documents/{documentId}")
    @Operation(
            summary = "Get audit logs for a document"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getByDocumentId(
            @PathVariable
            @Positive(
                    message =
                            "Document ID must be positive"
            )
            Long documentId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document audit logs retrieved successfully",
                        verificationAuditService
                                .getAuditLogsByDocumentId(
                                        documentId
                                )
                )
        );
    }

    @GetMapping("/ocr-results/{ocrResultId}")
    @Operation(
            summary = "Get audit logs for an OCR result"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getByOcrResultId(
            @PathVariable
            @Positive(
                    message =
                            "OCR result ID must be positive"
            )
            Long ocrResultId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OCR-result audit logs retrieved successfully",
                        verificationAuditService
                                .getAuditLogsByOcrResultId(
                                        ocrResultId
                                )
                )
        );
    }

    @GetMapping("/users/{username}")
    @Operation(
            summary = "Get audit logs for a user"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getByUsername(
            @PathVariable
            @NotBlank(
                    message = "Username is required"
            )
            String username) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User audit logs retrieved successfully",
                        verificationAuditService
                                .getAuditLogsByUsername(
                                        username
                                )
                )
        );
    }

    @GetMapping("/correlations/{correlationId}")
    @Operation(
            summary = "Get audit logs for a correlation ID"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationAuditLogResponse>>>
    getByCorrelationId(
            @PathVariable
            @NotBlank(
                    message =
                            "Correlation ID is required"
            )
            String correlationId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Correlation audit logs retrieved successfully",
                        verificationAuditService
                                .getAuditLogsByCorrelationId(
                                        correlationId
                                )
                )
        );
    }
}