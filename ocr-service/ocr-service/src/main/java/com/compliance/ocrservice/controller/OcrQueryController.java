package com.compliance.ocrservice.controller;

import com.compliance.ocrservice.dto.response.ApiResponse;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.dto.response.OcrSummaryResponse;
import com.compliance.ocrservice.enums.OcrStatus;
import com.compliance.ocrservice.service.interfaces.OcrService;
import com.compliance.ocrservice.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr/results")
@Validated
@Tag(
        name = "OCR Results",
        description = "Retrieve OCR results, histories, "
                + "status filters, and summaries"
)
@SecurityRequirement(name = "bearerAuth")
public class OcrQueryController {

    private final OcrService ocrService;
    private final SecurityContextUtil securityContextUtil;

    public OcrQueryController(
            OcrService ocrService,
            SecurityContextUtil securityContextUtil) {

        this.ocrService = ocrService;
        this.securityContextUtil =
                securityContextUtil;
    }

    @GetMapping("/{ocrResultId}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get OCR result by ID",
            description = "Returns the complete OCR result, "
                    + "including extracted text"
    )
    public ResponseEntity<ApiResponse<OcrResultResponse>>
    getResultById(
            @PathVariable("ocrResultId")
            @Positive(
                    message = "OCR result ID must be positive"
            )
            Long ocrResultId) {

        OcrResultResponse result =
                ocrService.getResultById(
                        ocrResultId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OCR result retrieved successfully",
                        result
                )
        );
    }

    @GetMapping("/document/{documentId}/latest")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get latest OCR result for a document",
            description = "Returns the most recent active OCR result "
                    + "for the specified document"
    )
    public ResponseEntity<ApiResponse<OcrResultResponse>>
    getLatestResultByDocumentId(
            @PathVariable("documentId")
            @Positive(
                    message = "Document ID must be positive"
            )
            Long documentId) {

        OcrResultResponse result =
                ocrService
                        .getLatestResultByDocumentId(
                                documentId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Latest OCR result retrieved successfully",
                        result
                )
        );
    }

    @GetMapping("/document/{documentId}/history")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get OCR history for a document",
            description = "Returns all active OCR attempts "
                    + "for the specified document"
    )
    public ResponseEntity
            <ApiResponse<List<OcrSummaryResponse>>>
    getHistoryByDocumentId(
            @PathVariable("documentId")
            @Positive(
                    message = "Document ID must be positive"
            )
            Long documentId) {

        List<OcrSummaryResponse> results =
                ocrService
                        .getHistoryByDocumentId(
                                documentId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OCR history retrieved successfully",
                        results
                )
        );
    }

    @GetMapping("/me")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get current user's OCR results",
            description = "Returns OCR requests submitted "
                    + "by the authenticated user"
    )
    public ResponseEntity
            <ApiResponse<List<OcrSummaryResponse>>>
    getCurrentUserResults() {

        String username =
                securityContextUtil
                        .getCurrentUsername();

        List<OcrSummaryResponse> results =
                ocrService
                        .getResultsByRequestedUser(
                                username
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User OCR results retrieved successfully",
                        results
                )
        );
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Filter OCR results by status",
            description = "Returns OCR results matching "
                    + "the requested processing status"
    )
    public ResponseEntity
            <ApiResponse<List<OcrSummaryResponse>>>
    getResultsByStatus(
            @PathVariable("status")
            OcrStatus status) {

        List<OcrSummaryResponse> results =
                ocrService.getResultsByStatus(
                        status
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OCR results filtered successfully",
                        results
                )
        );
    }

    @GetMapping("/summary")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get OCR status summary",
            description = "Returns the total number of active OCR "
                    + "results and counts grouped by status"
    )
    public ResponseEntity<ApiResponse<Map<String, Long>>>
    getStatusSummary() {

        Map<String, Long> summary =
                ocrService.getStatusSummary();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OCR status summary retrieved successfully",
                        summary
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'VERIFIER', 'AUDITOR')"
    )
    @Operation(
            summary = "Get all OCR results",
            description = "Returns summaries of all active OCR results"
    )
    public ResponseEntity
            <ApiResponse<List<OcrSummaryResponse>>>
    getAllResults() {

        List<OcrSummaryResponse> results =
                ocrService.getAllResults();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "OCR results retrieved successfully",
                        results
                )
        );
    }
}