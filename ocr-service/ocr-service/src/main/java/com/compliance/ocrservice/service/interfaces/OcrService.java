package com.compliance.ocrservice.service.interfaces;

import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.dto.response.OcrSummaryResponse;
import com.compliance.ocrservice.enums.OcrStatus;
import com.compliance.ocrservice.ocr.OcrDocumentContent;

import java.util.List;
import java.util.Map;

public interface OcrService {

    OcrResultResponse processDocument(
            OcrDocumentContent documentContent,
            String language,
            String requestedBy,
            boolean forceReprocess
    );

    OcrResultResponse retryOcr(
            Long ocrResultId,
            OcrDocumentContent documentContent,
            String language,
            String requestedBy,
            String retryReason
    );

    OcrResultResponse getResultById(
            Long ocrResultId
    );

    OcrResultResponse getLatestResultByDocumentId(
            Long documentId
    );

    List<OcrSummaryResponse>
    getHistoryByDocumentId(
            Long documentId
    );

    List<OcrSummaryResponse>
    getResultsByRequestedUser(
            String requestedBy
    );

    List<OcrSummaryResponse>
    getResultsByStatus(
            OcrStatus status
    );

    List<OcrSummaryResponse>
    getAllResults();

    Map<String, Long> getStatusSummary();
}