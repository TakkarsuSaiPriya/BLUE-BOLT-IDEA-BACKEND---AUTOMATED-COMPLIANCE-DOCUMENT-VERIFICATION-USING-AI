package com.compliance.ocrservice.ocr;

public class OcrProcessingResult {

    private String extractedText;
    private double averageConfidence;
    private int pageCount;
    private int characterCount;
    private int wordCount;
    private long processingDurationMs;

    public OcrProcessingResult() {
    }

    public OcrProcessingResult(
            String extractedText,
            double averageConfidence,
            int pageCount,
            int characterCount,
            int wordCount,
            long processingDurationMs) {

        this.extractedText = extractedText;
        this.averageConfidence =
                averageConfidence;
        this.pageCount = pageCount;
        this.characterCount =
                characterCount;
        this.wordCount = wordCount;
        this.processingDurationMs =
                processingDurationMs;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(
            String extractedText) {

        this.extractedText = extractedText;
    }

    public double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(
            double averageConfidence) {

        this.averageConfidence =
                averageConfidence;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(
            int pageCount) {

        this.pageCount = pageCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(
            int characterCount) {

        this.characterCount =
                characterCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(
            int wordCount) {

        this.wordCount = wordCount;
    }

    public long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(
            long processingDurationMs) {

        this.processingDurationMs =
                processingDurationMs;
    }
}