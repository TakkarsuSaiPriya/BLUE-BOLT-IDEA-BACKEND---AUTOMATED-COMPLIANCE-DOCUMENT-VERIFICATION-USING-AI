package com.compliance.verificationservice.dto.event;

import com.compliance.verificationservice.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VerificationCompletedEvent {

    private String eventId;
    private String eventType;
    private Long verificationResultId;
    private Long documentId;
    private Long ocrResultId;
    private VerificationStatus status;
    private BigDecimal verificationScore;
    private int mandatoryFailureCount;
    private String requestedBy;
    private LocalDateTime occurredAt;

    public VerificationCompletedEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(
            String eventId) {

        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(
            String eventType) {

        this.eventType = eventType;
    }

    public Long getVerificationResultId() {
        return verificationResultId;
    }

    public void setVerificationResultId(
            Long verificationResultId) {

        this.verificationResultId =
                verificationResultId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId = ocrResultId;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(
            VerificationStatus status) {

        this.status = status;
    }

    public BigDecimal getVerificationScore() {
        return verificationScore;
    }

    public void setVerificationScore(
            BigDecimal verificationScore) {

        this.verificationScore =
                verificationScore;
    }

    public int getMandatoryFailureCount() {
        return mandatoryFailureCount;
    }

    public void setMandatoryFailureCount(
            int mandatoryFailureCount) {

        this.mandatoryFailureCount =
                mandatoryFailureCount;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(
            String requestedBy) {

        this.requestedBy = requestedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}