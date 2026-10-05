package com.compliance.ocrservice.enums;

public enum OcrStatus {

    PENDING,
    DOWNLOADING,
    PROCESSING,
    COMPLETED,
    LOW_CONFIDENCE,
    FAILED,
    RETRY_PENDING
}