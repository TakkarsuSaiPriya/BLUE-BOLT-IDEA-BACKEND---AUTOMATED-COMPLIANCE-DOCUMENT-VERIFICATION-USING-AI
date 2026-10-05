package com.compliance.ocrservice.dto.response;

import com.compliance.ocrservice.enums.OcrEngine;
import com.compliance.ocrservice.enums.OcrStatus;

import java.time.LocalDateTime;

public class OcrSummaryResponse {

    private Long id;
    private Long documentId;
    private String originalFileName;
    private OcrEngine engine;
    private OcrStatus status;
    private String language;
    private Double averageConfidence;
    private Integer pageCount;
    private Integer characterCount;
    private Integer wordCount;
    private Long processingDurationMs;
    private String requestedBy;
    private int retryCount;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public OcrSummaryResponse() {
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

        this.originalFileName = originalFileName;
    }

    public OcrEngine getEngine() {
        return engine;
    }

    public void setEngine(
            OcrEngine engine) {

        this.engine = engine;
    }

    public OcrStatus getStatus() {
        return status;
    }

    public void setStatus(
            OcrStatus status) {

        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(
            String language) {

        this.language = language;
    }

    public Double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(
            Double averageConfidence) {

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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(
            int retryCount) {

        this.retryCount = retryCount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}