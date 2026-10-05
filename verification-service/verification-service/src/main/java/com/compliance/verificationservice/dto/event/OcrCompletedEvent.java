package com.compliance.verificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrCompletedEvent {

    private String eventId;
    private String eventType;
    private Long ocrResultId;
    private Long documentId;
    private String originalFileName;
    private String contentType;
    private String language;
    private String status;
    private BigDecimal averageConfidence;
    private Integer pageCount;
    private Integer characterCount;
    private Integer wordCount;
    private Long processingDurationMs;
    private String requestedBy;
    private LocalDateTime occurredAt;

    public OcrCompletedEvent() {
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

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public void setOcrResultId(
            Long ocrResultId) {

        this.ocrResultId = ocrResultId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName =
                originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType) {

        this.contentType = contentType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(
            String language) {

        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public BigDecimal getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(
            BigDecimal averageConfidence) {

        this.averageConfidence =
                averageConfidence;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(
            Integer pageCount) {

        this.pageCount = pageCount;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(
            Integer characterCount) {

        this.characterCount =
                characterCount;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(
            Integer wordCount) {

        this.wordCount = wordCount;
    }

    public Long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(
            Long processingDurationMs) {

        this.processingDurationMs =
                processingDurationMs;
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