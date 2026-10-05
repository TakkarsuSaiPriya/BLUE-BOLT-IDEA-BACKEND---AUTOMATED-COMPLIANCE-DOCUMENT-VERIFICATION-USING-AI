package com.compliance.verificationservice.controller;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ManualVerificationRequest;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.request.VerificationRetryRequest;
import com.compliance.verificationservice.dto.response.ApiResponse;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.dto.response.VerificationSummaryResponse;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.service.interfaces.OcrResultService;
import com.compliance.verificationservice.service.interfaces.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/verifications")
@Validated
@Tag(
        name = "Verification",
        description = "Document compliance verification operations"
)
public class VerificationController {

    private final VerificationService
            verificationService;

    private final OcrResultService
            ocrResultService;

    public VerificationController(
            VerificationService verificationService,
            OcrResultService ocrResultService) {

        this.verificationService =
                verificationService;

        this.ocrResultService =
                ocrResultService;
    }

    @PostMapping("/manual")
    @Operation(
            summary = "Start manual verification",
            description =
                    "Retrieves an existing OCR result and runs "
                            + "the verification rule engine"
    )
    public ResponseEntity<
            ApiResponse<VerificationResultResponse>>
    processManualVerification(
            @Valid
            @RequestBody
            ManualVerificationRequest request,
            Authentication authentication) {

        String requestedBy =
                getAuthenticatedUsername(
                        authentication
                );

        OcrResultInternalResponse ocrResult =
                ocrResultService.getOcrResult(
                        request.getOcrResultId()
                );

        VerificationResultResponse result =
                verificationService.processVerification(
                        ocrResult,
                        normalizeDocumentType(
                                request.getDocumentType()
                        ),
                        requestedBy,
                        request.isForceReprocess()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        ApiResponse.success(
                                "Verification completed successfully",
                                result
                        )
                );
    }

    @PostMapping("/{verificationResultId}/retry")
    @Operation(
            summary = "Retry verification",
            description =
                    "Creates a new verification attempt using "
                            + "the existing attempt's OCR result"
    )
    public ResponseEntity<
            ApiResponse<VerificationResultResponse>>
    retryVerification(
            @PathVariable
            @Positive(
                    message =
                            "Verification result ID must be positive"
            )
            Long verificationResultId,
            @Valid
            @RequestBody
            VerificationRetryRequest request,
            Authentication authentication) {

        String requestedBy =
                getAuthenticatedUsername(
                        authentication
                );

        VerificationResultResponse existingResult =
                verificationService.getVerificationResult(
                        verificationResultId
                );

        OcrResultInternalResponse ocrResult =
                ocrResultService.getOcrResult(
                        existingResult.getOcrResultId()
                );

        VerificationResultResponse result =
                verificationService.retryVerification(
                        verificationResultId,
                        ocrResult,
                        requestedBy,
                        request.getReason()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification retry completed successfully",
                        result
                )
        );
    }

    @PatchMapping("/{verificationResultId}/review")
    @Operation(
            summary = "Review verification result",
            description =
                    "Applies an authorized manual decision to a "
                            + "REVIEW_REQUIRED result"
    )
    public ResponseEntity<
            ApiResponse<VerificationResultResponse>>
    reviewVerification(
            @PathVariable
            @Positive(
                    message =
                            "Verification result ID must be positive"
            )
            Long verificationResultId,
            @Valid
            @RequestBody
            ReviewDecisionRequest request,
            Authentication authentication) {

        String reviewedBy =
                getAuthenticatedUsername(
                        authentication
                );

        VerificationResultResponse result =
                verificationService.reviewVerification(
                        verificationResultId,
                        request,
                        reviewedBy
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification review completed successfully",
                        result
                )
        );
    }

    @GetMapping("/{verificationResultId}")
    @Operation(
            summary = "Get verification result"
    )
    public ResponseEntity<
            ApiResponse<VerificationResultResponse>>
    getVerificationResult(
            @PathVariable
            @Positive(
                    message =
                            "Verification result ID must be positive"
            )
            Long verificationResultId) {

        VerificationResultResponse result =
                verificationService.getVerificationResult(
                        verificationResultId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification result retrieved successfully",
                        result
                )
        );
    }

    @GetMapping("/documents/{documentId}/latest")
    @Operation(
            summary = "Get latest document verification"
    )
    public ResponseEntity<
            ApiResponse<VerificationResultResponse>>
    getLatestDocumentVerification(
            @PathVariable
            @Positive(
                    message =
                            "Document ID must be positive"
            )
            Long documentId) {

        VerificationResultResponse result =
                verificationService.getLatestByDocumentId(
                        documentId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Latest document verification retrieved successfully",
                        result
                )
        );
    }

    @GetMapping("/documents/{documentId}/history")
    @Operation(
            summary = "Get document verification history"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationResultResponse>>>
    getDocumentVerificationHistory(
            @PathVariable
            @Positive(
                    message =
                            "Document ID must be positive"
            )
            Long documentId) {

        List<VerificationResultResponse> results =
                verificationService.getDocumentHistory(
                        documentId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document verification history retrieved successfully",
                        results
                )
        );
    }

    @GetMapping("/users/{username}")
    @Operation(
            summary = "Get results for a user"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationResultResponse>>>
    getResultsForUser(
            @PathVariable
            @NotBlank(
                    message =
                            "Username is required"
            )
            String username) {

        List<VerificationResultResponse> results =
                verificationService.getResultsForUser(
                        normalizeUsername(
                                username
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User verification results retrieved successfully",
                        results
                )
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user's verification results"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationResultResponse>>>
    getCurrentUserResults(
            Authentication authentication) {

        String username =
                getAuthenticatedUsername(
                        authentication
                );

        List<VerificationResultResponse> results =
                verificationService.getResultsForUser(
                        username
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user verification results retrieved successfully",
                        results
                )
        );
    }

    @GetMapping
    @Operation(
            summary = "List verification results"
    )
    public ResponseEntity<
            ApiResponse<List<VerificationResultResponse>>>
    getVerificationResults(
            @RequestParam(
                    value = "status",
                    required = false
            )
            VerificationStatus status) {

        List<VerificationResultResponse> results;

        String message;

        if (status == null) {

            results =
                    verificationService.getAllResults();

            message =
                    "Verification results retrieved successfully";

        } else {

            results =
                    verificationService.getResultsByStatus(
                            status
                    );

            message =
                    "Verification results with status "
                            + status
                            + " retrieved successfully";
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        results
                )
        );
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get verification status summary"
    )
    public ResponseEntity<
            ApiResponse<VerificationSummaryResponse>>
    getVerificationSummary() {

        VerificationSummaryResponse summary =
                verificationService.getStatusSummary();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification summary retrieved successfully",
                        summary
                )
        );
    }

    private String getAuthenticatedUsername(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Authenticated username is unavailable"
            );
        }

        return normalizeUsername(
                authentication.getName()
        );
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Username is required"
            );
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

    private String normalizeDocumentType(
            String documentType) {

        if (documentType == null
                || documentType.isBlank()) {

            return "GENERAL";
        }

        String normalized =
                documentType.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalized.length() > 100) {
            return normalized.substring(
                    0,
                    100
            );
        }

        return normalized;
    }
}