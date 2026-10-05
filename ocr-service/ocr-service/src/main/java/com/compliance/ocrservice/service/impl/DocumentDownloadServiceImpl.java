package com.compliance.ocrservice.service.impl;

import com.compliance.ocrservice.client.DocumentServiceClient;
import com.compliance.ocrservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.ocrservice.exception.DocumentDownloadException;
import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.ocr.OcrDocumentContent;
import com.compliance.ocrservice.service.interfaces.DocumentDownloadService;
import feign.FeignException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DocumentDownloadServiceImpl
        implements DocumentDownloadService {

    private static final String OCR_SERVICE_NAME =
            "ocr-service";

    private static final String DOCUMENT_ID_HEADER =
            "X-Document-Id";

    private static final String ORIGINAL_FILE_NAME_HEADER =
            "X-Document-Original-File-Name";

    private final DocumentServiceClient documentServiceClient;

    public DocumentDownloadServiceImpl(
            DocumentServiceClient documentServiceClient) {

        this.documentServiceClient =
                documentServiceClient;
    }

    @Override
    public OcrDocumentContent downloadDocument(
            Long documentId) {

        validateDocumentId(documentId);

        try {
            ResponseEntity<byte[]> response =
                    documentServiceClient
                            .downloadDocumentContent(
                                    documentId
                            );

            validateDownloadResponse(
                    documentId,
                    response
            );

            HttpHeaders headers =
                    response.getHeaders();

            byte[] content =
                    response.getBody();

            return new OcrDocumentContent(
                    resolveDocumentId(
                            documentId,
                            headers
                    ),
                    resolveOriginalFileName(
                            headers
                    ),
                    resolveContentType(
                            headers
                    ),
                    content
            );

        } catch (FeignException.NotFound exception) {

            throw new DocumentDownloadException(
                    "Document not found with ID: "
                            + documentId,
                    exception
            );

        } catch (FeignException.Unauthorized exception) {

            throw new DocumentDownloadException(
                    "Document Service rejected "
                            + "OCR Service authentication",
                    exception
            );

        } catch (FeignException.Forbidden exception) {

            throw new DocumentDownloadException(
                    "Document Service denied "
                            + "OCR Service access",
                    exception
            );

        } catch (FeignException.ServiceUnavailable exception) {

            throw new DocumentDownloadException(
                    "Document Service is currently unavailable",
                    exception
            );

        } catch (FeignException exception) {

            throw new DocumentDownloadException(
                    "Unable to download document with ID: "
                            + documentId
                            + ". Document Service returned status: "
                            + exception.status(),
                    exception
            );

        } catch (DocumentDownloadException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            throw new DocumentDownloadException(
                    documentId,
                    exception
            );
        }
    }

    @Override
    public void markOcrProcessing(
            Long documentId) {

        updateStatus(
                documentId,
                "OCR_PROCESSING",
                "OCR text extraction started"
        );
    }

    @Override
    public void markOcrCompleted(
            Long documentId,
            String reason) {

        updateStatus(
                documentId,
                "OCR_COMPLETED",
                normalizeReason(
                        reason,
                        "OCR text extraction completed"
                )
        );
    }

    @Override
    public void markOcrFailed(
            Long documentId,
            String reason) {

        updateStatus(
                documentId,
                "FAILED",
                normalizeReason(
                        reason,
                        "OCR text extraction failed"
                )
        );
    }

    private void updateStatus(
            Long documentId,
            String status,
            String reason) {

        validateDocumentId(documentId);

        DocumentStatusUpdateRequest request =
                new DocumentStatusUpdateRequest(
                        status,
                        limitReason(reason),
                        OCR_SERVICE_NAME
                );

        try {
            ResponseEntity<Void> response =
                    documentServiceClient
                            .updateDocumentStatus(
                                    documentId,
                                    request
                            );

            validateStatusUpdateResponse(
                    documentId,
                    response
            );

        } catch (FeignException.NotFound exception) {

            throw new DocumentDownloadException(
                    "Cannot update OCR status because "
                            + "document was not found with ID: "
                            + documentId,
                    exception
            );

        } catch (FeignException.Unauthorized exception) {

            throw new DocumentDownloadException(
                    "Document Service rejected "
                            + "OCR Service authentication",
                    exception
            );

        } catch (FeignException.Forbidden exception) {

            throw new DocumentDownloadException(
                    "Document Service denied "
                            + "the OCR status update request",
                    exception
            );

        } catch (FeignException.ServiceUnavailable exception) {

            throw new DocumentDownloadException(
                    "Document Service is currently unavailable",
                    exception
            );

        } catch (FeignException exception) {

            throw new DocumentDownloadException(
                    "Unable to update document status "
                            + "for document ID: "
                            + documentId
                            + ". Document Service returned status: "
                            + exception.status(),
                    exception
            );

        } catch (DocumentDownloadException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            throw new DocumentDownloadException(
                    "Unexpected error while updating "
                            + "document status for ID: "
                            + documentId,
                    exception
            );
        }
    }

    private void validateDownloadResponse(
            Long documentId,
            ResponseEntity<byte[]> response) {

        if (response == null) {
            throw new DocumentDownloadException(
                    "Document Service returned no response "
                            + "for document ID: "
                            + documentId
            );
        }

        if (!response.getStatusCode()
                .is2xxSuccessful()) {

            throw new DocumentDownloadException(
                    "Document Service returned an unsuccessful "
                            + "response for document ID: "
                            + documentId
            );
        }

        byte[] responseBody =
                response.getBody();

        if (responseBody == null
                || responseBody.length == 0) {

            throw new DocumentDownloadException(
                    "Document Service returned empty content "
                            + "for document ID: "
                            + documentId
            );
        }
    }

    private void validateStatusUpdateResponse(
            Long documentId,
            ResponseEntity<Void> response) {

        if (response == null) {
            throw new DocumentDownloadException(
                    "Document Service returned no response "
                            + "while updating status for document ID: "
                            + documentId
            );
        }

        if (!response.getStatusCode()
                .is2xxSuccessful()) {

            throw new DocumentDownloadException(
                    "Document Service returned an unsuccessful "
                            + "status-update response for document ID: "
                            + documentId
            );
        }
    }

    private String resolveOriginalFileName(
            HttpHeaders headers) {

        if (headers == null) {
            return "document";
        }

        String customFileName =
                headers.getFirst(
                        ORIGINAL_FILE_NAME_HEADER
                );

        if (customFileName != null
                && !customFileName.isBlank()) {

            return sanitizeFileName(
                    customFileName
            );
        }

        String contentDispositionHeader =
                headers.getFirst(
                        HttpHeaders.CONTENT_DISPOSITION
                );

        if (contentDispositionHeader != null
                && !contentDispositionHeader.isBlank()) {

            try {
                ContentDisposition disposition =
                        ContentDisposition.parse(
                                contentDispositionHeader
                        );

                String fileName =
                        disposition.getFilename();

                if (fileName != null
                        && !fileName.isBlank()) {

                    return sanitizeFileName(
                            fileName
                    );
                }

            } catch (IllegalArgumentException ignored) {
                // Return safe fallback below.
            }
        }

        return "document";
    }

    private String resolveContentType(
            HttpHeaders headers) {

        if (headers == null) {
            return MediaType
                    .APPLICATION_OCTET_STREAM_VALUE;
        }

        MediaType mediaType =
                headers.getContentType();

        if (mediaType == null) {
            return MediaType
                    .APPLICATION_OCTET_STREAM_VALUE;
        }

        return mediaType.toString();
    }

    private Long resolveDocumentId(
            Long requestedDocumentId,
            HttpHeaders headers) {

        if (headers == null) {
            return requestedDocumentId;
        }

        String documentIdHeader =
                headers.getFirst(
                        DOCUMENT_ID_HEADER
                );

        if (documentIdHeader == null
                || documentIdHeader.isBlank()) {

            return requestedDocumentId;
        }

        try {
            Long returnedDocumentId =
                    Long.valueOf(
                            documentIdHeader.trim()
                    );

            if (!requestedDocumentId.equals(
                    returnedDocumentId)) {

                throw new DocumentDownloadException(
                        "Document Service returned content "
                                + "for an unexpected document ID"
                );
            }

            return returnedDocumentId;

        } catch (NumberFormatException exception) {

            throw new DocumentDownloadException(
                    "Document Service returned an invalid "
                            + "document ID header",
                    exception
            );
        }
    }

    private String sanitizeFileName(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            return "document";
        }

        String sanitizedFileName =
                fileName.trim()
                        .replace("\\", "_")
                        .replace("/", "_")
                        .replace("\r", "")
                        .replace("\n", "");

        if (sanitizedFileName.isBlank()) {
            return "document";
        }

        if (sanitizedFileName.length() > 255) {
            return sanitizedFileName.substring(
                    0,
                    255
            );
        }

        return sanitizedFileName;
    }

    private String normalizeReason(
            String reason,
            String defaultReason) {

        if (reason == null
                || reason.isBlank()) {

            return defaultReason;
        }

        return reason.trim();
    }

    private String limitReason(
            String reason) {

        if (reason == null
                || reason.isBlank()) {

            return null;
        }

        String normalizedReason =
                reason.trim();

        if (normalizedReason.length() > 500) {
            return normalizedReason.substring(
                    0,
                    500
            );
        }

        return normalizedReason;
    }

    private void validateDocumentId(
            Long documentId) {

        if (documentId == null
                || documentId <= 0) {

            throw new InvalidOcrRequestException(
                    "Document ID must be a positive number"
            );
        }
    }
}