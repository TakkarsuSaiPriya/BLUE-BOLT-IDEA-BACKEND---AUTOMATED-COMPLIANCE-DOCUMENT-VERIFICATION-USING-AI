package com.compliance.ocrservice.mapper;

import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.dto.response.OcrSummaryResponse;
import com.compliance.ocrservice.entity.OcrResult;
import org.springframework.stereotype.Component;

@Component
public class OcrResultMapper {

    public OcrResultResponse toResponse(
            OcrResult result) {

        if (result == null) {
            return null;
        }

        OcrResultResponse response =
                new OcrResultResponse();

        response.setId(result.getId());

        response.setDocumentId(
                result.getDocumentId()
        );

        response.setOriginalFileName(
                result.getOriginalFileName()
        );

        response.setContentType(
                result.getContentType()
        );

        response.setEngine(
                result.getEngine()
        );

        response.setStatus(
                result.getStatus()
        );

        response.setLanguage(
                result.getLanguage()
        );

        response.setExtractedText(
                result.getExtractedText()
        );

        response.setAverageConfidence(
                result.getAverageConfidence()
        );

        response.setPageCount(
                result.getPageCount()
        );

        response.setCharacterCount(
                result.getCharacterCount()
        );

        response.setWordCount(
                result.getWordCount()
        );

        response.setProcessingDurationMs(
                result.getProcessingDurationMs()
        );

        response.setRequestedBy(
                result.getRequestedBy()
        );

        response.setStartedAt(
                result.getStartedAt()
        );

        response.setCompletedAt(
                result.getCompletedAt()
        );

        response.setRetryCount(
                result.getRetryCount()
        );

        response.setErrorMessage(
                result.getErrorMessage()
        );

        response.setActive(
                result.isActive()
        );

        response.setCreatedAt(
                result.getCreatedAt()
        );

        response.setUpdatedAt(
                result.getUpdatedAt()
        );

        return response;
    }

    public OcrSummaryResponse toSummaryResponse(
            OcrResult result) {

        if (result == null) {
            return null;
        }

        OcrSummaryResponse response =
                new OcrSummaryResponse();

        response.setId(result.getId());

        response.setDocumentId(
                result.getDocumentId()
        );

        response.setOriginalFileName(
                result.getOriginalFileName()
        );

        response.setEngine(
                result.getEngine()
        );

        response.setStatus(
                result.getStatus()
        );

        response.setLanguage(
                result.getLanguage()
        );

        response.setAverageConfidence(
                result.getAverageConfidence()
        );

        response.setPageCount(
                result.getPageCount()
        );

        response.setCharacterCount(
                result.getCharacterCount()
        );

        response.setWordCount(
                result.getWordCount()
        );

        response.setProcessingDurationMs(
                result.getProcessingDurationMs()
        );

        response.setRequestedBy(
                result.getRequestedBy()
        );

        response.setRetryCount(
                result.getRetryCount()
        );

        response.setCompletedAt(
                result.getCompletedAt()
        );

        response.setCreatedAt(
                result.getCreatedAt()
        );

        return response;
    }
}