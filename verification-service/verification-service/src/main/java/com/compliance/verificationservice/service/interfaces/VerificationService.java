package com.compliance.verificationservice.service.interfaces;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.dto.response.VerificationSummaryResponse;
import com.compliance.verificationservice.enums.VerificationStatus;

import java.util.List;

public interface VerificationService {

    VerificationResultResponse processVerification(
            OcrResultInternalResponse ocrResult,
            String documentType,
            String requestedBy,
            boolean forceReprocess
    );

    VerificationResultResponse retryVerification(
            Long verificationResultId,
            OcrResultInternalResponse ocrResult,
            String requestedBy,
            String reason
    );

    VerificationResultResponse reviewVerification(
            Long verificationResultId,
            ReviewDecisionRequest request,
            String reviewedBy
    );

    VerificationResultResponse getVerificationResult(
            Long verificationResultId
    );

    VerificationResultResponse getLatestByDocumentId(
            Long documentId
    );

    List<VerificationResultResponse>
    getDocumentHistory(
            Long documentId
    );

    List<VerificationResultResponse>
    getResultsForUser(
            String username
    );

    List<VerificationResultResponse>
    getResultsByStatus(
            VerificationStatus status
    );

    List<VerificationResultResponse>
    getAllResults();

    VerificationSummaryResponse
    getStatusSummary();
}