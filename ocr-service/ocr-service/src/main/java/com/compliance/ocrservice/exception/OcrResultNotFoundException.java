package com.compliance.ocrservice.exception;

public class OcrResultNotFoundException
        extends RuntimeException {

    public OcrResultNotFoundException(
            String message) {

        super(message);
    }

    public OcrResultNotFoundException(
            Long ocrResultId) {

        super(
                "OCR result not found with ID: "
                        + ocrResultId
        );
    }

    public static OcrResultNotFoundException
    forDocument(Long documentId) {

        return new OcrResultNotFoundException(
                "OCR result not found for document ID: "
                        + documentId
        );
    }
}