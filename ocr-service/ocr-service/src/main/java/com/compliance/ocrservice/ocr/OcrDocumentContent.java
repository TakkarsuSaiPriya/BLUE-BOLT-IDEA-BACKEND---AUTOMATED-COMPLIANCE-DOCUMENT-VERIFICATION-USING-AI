package com.compliance.ocrservice.ocr;

public class OcrDocumentContent {

    private Long documentId;
    private String originalFileName;
    private String contentType;
    private byte[] content;

    public OcrDocumentContent() {
    }

    public OcrDocumentContent(
            Long documentId,
            String originalFileName,
            String contentType,
            byte[] content) {

        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.content = content;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(
            byte[] content) {

        this.content = content;
    }

    public long getContentLength() {

        if (content == null) {
            return 0L;
        }

        return content.length;
    }
}