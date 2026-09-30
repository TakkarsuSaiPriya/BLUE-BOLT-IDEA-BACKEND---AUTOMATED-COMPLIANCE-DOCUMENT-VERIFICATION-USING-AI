package com.compliance.documentservice.enums;

public enum DocumentStatus {

    UPLOADED,
    QUEUED_FOR_OCR,
    OCR_PROCESSING,
    OCR_COMPLETED,
    VERIFICATION_PROCESSING,
    VERIFIED,
    APPROVED,
    REJECTED,
    FAILED
}