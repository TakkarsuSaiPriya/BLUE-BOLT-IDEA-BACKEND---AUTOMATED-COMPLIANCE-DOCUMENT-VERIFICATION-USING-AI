package com.compliance.verificationservice.producer;

import com.compliance.verificationservice.dto.response.VerificationResultResponse;

public interface VerificationEventProducer {

    void publishVerificationCompleted(
            VerificationResultResponse result
    );

    void publishVerificationFailed(
            Long verificationResultId,
            Long documentId,
            Long ocrResultId,
            String failureReason,
            String requestedBy
    );
}