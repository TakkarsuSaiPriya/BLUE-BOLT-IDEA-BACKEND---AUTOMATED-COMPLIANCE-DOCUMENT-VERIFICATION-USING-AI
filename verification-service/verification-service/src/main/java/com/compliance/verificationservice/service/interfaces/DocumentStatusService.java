package com.compliance.verificationservice.service.interfaces;

import com.compliance.verificationservice.enums.VerificationStatus;

public interface DocumentStatusService {

    void markVerificationProcessing(
            Long documentId,
            String requestedBy
    );

    void markVerificationCompleted(
            Long documentId,
            VerificationStatus verificationStatus,
            String reason,
            String requestedBy
    );

    void markVerificationFailed(
            Long documentId,
            String reason,
            String requestedBy
    );

    void updateDocumentStatus(
            Long documentId,
            String status,
            String reason,
            String changedBy
    );
}