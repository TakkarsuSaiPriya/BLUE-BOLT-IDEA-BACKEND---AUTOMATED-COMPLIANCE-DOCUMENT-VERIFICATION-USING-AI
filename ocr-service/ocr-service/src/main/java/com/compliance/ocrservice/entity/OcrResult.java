package com.compliance.ocrservice.entity;

import com.compliance.ocrservice.enums.OcrEngine;
import com.compliance.ocrservice.enums.OcrStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ocr_results",
        indexes = {
                @Index(
                        name = "idx_ocr_document_id",
                        columnList = "document_id"
                ),
                @Index(
                        name = "idx_ocr_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_ocr_requested_by",
                        columnList = "requested_by"
                )
        }
)
public class OcrResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "document_id",
            nullable = false
    )
    private Long documentId;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "engine",
            nullable = false,
            length = 30
    )
    private OcrEngine engine;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private OcrStatus status;

    @Column(
            name = "language",
            nullable = false,
            length = 30
    )
    private String language;

    @Lob
    @Column(
            name = "extracted_text",
            columnDefinition = "LONGTEXT"
    )
    private String extractedText;

    @Column(
            name = "average_confidence"
    )
    private Double averageConfidence;

    @Column(
            name = "page_count"
    )
    private Integer pageCount;

    @Column(
            name = "character_count"
    )
    private Integer characterCount;

    @Column(
            name = "word_count"
    )
    private Integer wordCount;

    @Column(
            name = "processing_duration_ms"
    )
    private Long processingDurationMs;

    @Column(
            name = "requested_by",
            nullable = false,
            length = 100
    )
    private String requestedBy;

    @Column(
            name = "started_at"
    )
    private LocalDateTime startedAt;

    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;

    @Column(
            name = "retry_count",
            nullable = false
    )
    private int retryCount = 0;

    @Column(
            name = "error_message",
            length = 2000
    )
    private String errorMessage;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    public OcrResult() {
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType) {

        this.contentType = contentType;
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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(
            String extractedText) {

        this.extractedText = extractedText;
    }

    public Double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(
            Double averageConfidence) {

        this.averageConfidence = averageConfidence;
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

        this.characterCount = characterCount;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(
            LocalDateTime startedAt) {

        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(
            int retryCount) {

        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active) {

        this.active = active;
    }
}