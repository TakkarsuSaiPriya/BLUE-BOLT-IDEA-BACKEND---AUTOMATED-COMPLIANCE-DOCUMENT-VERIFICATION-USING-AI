package com.compliance.verificationservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResultInternalResponse {

    private Long id;
    private Long documentId;
    private String originalFileName;
    private String contentType;
    private String language;
    private String status;
    private String extractedText;
    private BigDecimal averageConfidence;
    private Integer pageCount;
    private Integer characterCount;
    private Integer wordCount;
    private Long processingDurationMs;
    private String requestedBy;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public OcrResultInternalResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(
            String extractedText) {

        this.extractedText = extractedText;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(
            Integer retryCount) {

        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }
}