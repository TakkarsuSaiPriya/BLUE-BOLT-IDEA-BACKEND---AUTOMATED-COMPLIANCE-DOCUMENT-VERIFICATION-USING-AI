package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.client.DocumentServiceClient;
import com.compliance.verificationservice.dto.internal.DocumentStatusUpdateRequest;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.exception.ExternalServiceException;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.service.interfaces.DocumentStatusService;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class DocumentStatusServiceImpl
        implements DocumentStatusService {

    private static final String SERVICE_NAME =
            "document-service";

    private static final String DEFAULT_CHANGED_BY =
            "verification-service";

    private final DocumentServiceClient
            documentServiceClient;

    public DocumentStatusServiceImpl(
            DocumentServiceClient
                    documentServiceClient) {

        this.documentServiceClient =
                documentServiceClient;
    }

    @Override
    public void markVerificationProcessing(
            Long documentId,
            String requestedBy) {

        updateDocumentStatus(
                documentId,
                "VERIFICATION_PROCESSING",
                "Document verification processing started",
                resolveChangedBy(
                        requestedBy
                )
        );
    }

    @Override
    public void markVerificationCompleted(
            Long documentId,
            VerificationStatus verificationStatus,
            String reason,
            String requestedBy) {

        if (verificationStatus == null) {

            throw new InvalidVerificationRequestException(
                    "Verification status is required"
            );
        }

        String documentStatus =
                mapCompletedStatus(
                        verificationStatus
                );

        String completionReason =
                reason == null
                        || reason.isBlank()
                        ? "Document verification completed with status "
                        + verificationStatus
                        : reason.trim();

        updateDocumentStatus(
                documentId,
                documentStatus,
                completionReason,
                resolveChangedBy(
                        requestedBy
                )
        );
    }

    @Override
    public void markVerificationFailed(
            Long documentId,
            String reason,
            String requestedBy) {

        String failureReason =
                reason == null
                        || reason.isBlank()
                        ? "Document verification failed"
                        : reason.trim();

        updateDocumentStatus(
                documentId,
                "VERIFICATION_FAILED",
                failureReason,
                resolveChangedBy(
                        requestedBy
                )
        );
    }

    @Override
    public void updateDocumentStatus(
            Long documentId,
            String status,
            String reason,
            String changedBy) {

        validateDocumentId(
                documentId
        );

        String normalizedStatus =
                normalizeStatus(
                        status
                );

        String normalizedReason =
                normalizeLimitedText(
                        reason,
                        1000
                );

        String normalizedChangedBy =
                normalizeChangedBy(
                        changedBy
                );

        DocumentStatusUpdateRequest request =
                new DocumentStatusUpdateRequest(
                        normalizedStatus,
                        normalizedReason,
                        normalizedChangedBy
                );

        try {

            ResponseEntity<Void> response =
                    documentServiceClient
                            .updateDocumentStatus(
                                    documentId,
                                    request
                            );

            if (response == null) {

                throw new ExternalServiceException(
                        SERVICE_NAME,
                        "Document Service returned no response "
                                + "while updating document ID: "
                                + documentId
                );
            }

            if (!response.getStatusCode()
                    .is2xxSuccessful()) {

                throw new ExternalServiceException(
                        SERVICE_NAME,
                        response.getStatusCode().value(),
                        "Document Service returned an unsuccessful "
                                + "response while updating document ID: "
                                + documentId
                );
            }

        } catch (ExternalServiceException exception) {

            throw exception;

        } catch (FeignException exception) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    exception.status(),
                    "Unable to update status for document ID: "
                            + documentId,
                    exception
            );

        } catch (Exception exception) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "Unexpected error while updating document ID: "
                            + documentId,
                    exception
            );
        }
    }

    private String mapCompletedStatus(
            VerificationStatus verificationStatus) {

        return switch (verificationStatus) {

            case VERIFIED ->
                    "VERIFIED";

            case REJECTED ->
                    "VERIFICATION_REJECTED";

            case REVIEW_REQUIRED ->
                    "REVIEW_REQUIRED";

            case FAILED ->
                    "VERIFICATION_FAILED";

            case PENDING ->
                    "VERIFICATION_PENDING";

            case PROCESSING ->
                    "VERIFICATION_PROCESSING";
        };
    }

    private void validateDocumentId(
            Long documentId) {

        if (documentId == null
                || documentId <= 0) {

            throw new InvalidVerificationRequestException(
                    "Document ID must be a positive number"
            );
        }
    }

    private String normalizeStatus(
            String status) {

        if (status == null
                || status.isBlank()) {

            throw new InvalidVerificationRequestException(
                    "Document status is required"
            );
        }

        String normalizedStatus =
                status.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (normalizedStatus.length() > 100) {

            throw new InvalidVerificationRequestException(
                    "Document status cannot exceed 100 characters"
            );
        }

        return normalizedStatus;
    }

    private String resolveChangedBy(
            String requestedBy) {

        if (requestedBy == null
                || requestedBy.isBlank()
                || "system".equalsIgnoreCase(
                requestedBy.trim())) {

            return DEFAULT_CHANGED_BY;
        }

        return normalizeChangedBy(
                requestedBy
        );
    }

    private String normalizeChangedBy(
            String changedBy) {

        if (changedBy == null
                || changedBy.isBlank()) {

            return DEFAULT_CHANGED_BY;
        }

        String normalizedChangedBy =
                changedBy.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return normalizeLimitedText(
                normalizedChangedBy,
                150
        );
    }

    private String normalizeLimitedText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        String normalizedValue =
                value.trim();

        if (normalizedValue.length()
                > maximumLength) {

            return normalizedValue.substring(
                    0,
                    maximumLength
            );
        }

        return normalizedValue;
    }
}