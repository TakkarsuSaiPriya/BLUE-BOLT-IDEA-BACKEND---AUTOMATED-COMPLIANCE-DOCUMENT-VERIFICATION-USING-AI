package com.compliance.verificationservice.rule;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class VerificationContext {

    private final Long documentId;
    private final Long ocrResultId;
    private final String originalFileName;
    private final String documentType;
    private final String extractedText;
    private final String normalizedText;
    private final BigDecimal ocrConfidence;
    private final Integer pageCount;
    private final Integer characterCount;
    private final Integer wordCount;
    private final String requestedBy;
    private final Map<String, String> attributes;

    public VerificationContext(
            Long documentId,
            Long ocrResultId,
            String originalFileName,
            String documentType,
            String extractedText,
            BigDecimal ocrConfidence,
            Integer pageCount,
            Integer characterCount,
            Integer wordCount,
            String requestedBy,
            Map<String, String> attributes) {

        this.documentId = documentId;
        this.ocrResultId = ocrResultId;
        this.originalFileName =
                normalizeNullableText(
                        originalFileName
                );
        this.documentType =
                normalizeNullableText(
                        documentType
                );
        this.extractedText =
                extractedText == null
                        ? ""
                        : extractedText;
        this.normalizedText =
                normalizeOcrText(
                        extractedText
                );
        this.ocrConfidence =
                ocrConfidence;
        this.pageCount =
                normalizeNonNegativeInteger(
                        pageCount
                );
        this.characterCount =
                normalizeNonNegativeInteger(
                        characterCount
                );
        this.wordCount =
                normalizeNonNegativeInteger(
                        wordCount
                );
        this.requestedBy =
                normalizeUsername(
                        requestedBy
                );
        this.attributes =
                normalizeAttributes(
                        attributes
                );
    }

    public static VerificationContext fromOcrResult(
            OcrResultInternalResponse ocrResult,
            String documentType,
            String requestedBy) {

        if (ocrResult == null) {
            throw new IllegalArgumentException(
                    "OCR result is required"
            );
        }

        if (ocrResult.getId() == null
                || ocrResult.getId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid OCR result ID is required"
            );
        }

        if (ocrResult.getDocumentId() == null
                || ocrResult.getDocumentId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid document ID is required"
            );
        }

        return new VerificationContext(
                ocrResult.getDocumentId(),
                ocrResult.getId(),
                ocrResult.getOriginalFileName(),
                documentType,
                ocrResult.getExtractedText(),
                ocrResult.getAverageConfidence(),
                ocrResult.getPageCount(),
                ocrResult.getCharacterCount(),
                ocrResult.getWordCount(),
                requestedBy,
                Collections.emptyMap()
        );
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Long getOcrResultId() {
        return ocrResultId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public BigDecimal getOcrConfidence() {
        return ocrConfidence;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public Map<String, String> getAttributes() {

        return Collections.unmodifiableMap(
                attributes
        );
    }

    public String getAttribute(
            String key) {

        if (key == null
                || key.isBlank()) {

            return null;
        }

        return attributes.get(
                key.trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
        );
    }

    public boolean hasExtractedText() {

        return !normalizedText.isBlank();
    }

    public boolean containsIgnoreCase(
            String value) {

        if (value == null
                || value.isBlank()) {

            return false;
        }

        return normalizedText.contains(
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
        );
    }

    private String normalizeOcrText(
            String text) {

        if (text == null
                || text.isBlank()) {

            return "";
        }

        return text.replaceAll(
                        "\\s+",
                        " "
                )
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeNullableText(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private Integer normalizeNonNegativeInteger(
            Integer value) {

        if (value == null) {
            return null;
        }

        return Math.max(
                value,
                0
        );
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "system";
        }

        return username.trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private Map<String, String> normalizeAttributes(
            Map<String, String> source) {

        Map<String, String> normalized =
                new LinkedHashMap<>();

        if (source == null
                || source.isEmpty()) {

            return normalized;
        }

        for (Map.Entry<String, String> entry
                : source.entrySet()) {

            if (entry.getKey() == null
                    || entry.getKey().isBlank()) {

                continue;
            }

            String key =
                    entry.getKey()
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            String value =
                    entry.getValue() == null
                            ? null
                            : entry.getValue().trim();

            normalized.put(
                    key,
                    value
            );
        }

        return normalized;
    }
}