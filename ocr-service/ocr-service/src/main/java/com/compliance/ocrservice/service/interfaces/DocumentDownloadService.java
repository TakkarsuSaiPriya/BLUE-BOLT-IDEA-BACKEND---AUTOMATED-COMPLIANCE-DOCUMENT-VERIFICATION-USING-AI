package com.compliance.ocrservice.service.interfaces;

import com.compliance.ocrservice.ocr.OcrDocumentContent;

public interface DocumentDownloadService {

    OcrDocumentContent downloadDocument(
            Long documentId
    );

    void markOcrProcessing(
            Long documentId
    );

    void markOcrCompleted(
            Long documentId,
            String reason
    );

    void markOcrFailed(
            Long documentId,
            String reason
    );
}