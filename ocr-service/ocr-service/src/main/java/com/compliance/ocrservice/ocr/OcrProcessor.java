package com.compliance.ocrservice.ocr;

public interface OcrProcessor {

    boolean supports(String contentType);

    OcrProcessingResult process(
            byte[] documentContent,
            String originalFileName,
            String language
    );
}
