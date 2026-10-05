package com.compliance.verificationservice.service.interfaces;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;

public interface OcrResultService {

    OcrResultInternalResponse getOcrResult(
            Long ocrResultId
    );
}