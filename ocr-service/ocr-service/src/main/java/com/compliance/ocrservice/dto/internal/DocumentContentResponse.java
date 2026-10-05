package com.compliance.ocrservice.dto.internal;

public class DocumentContentResponse {

    private Long documentId;
    private String originalFileName;
    private String contentType;
    private long contentLength;
    private byte[] content;

    public DocumentContentResponse() {
    }

    public DocumentContentResponse(
            Long documentId,
            String originalFileName,
            String contentType,
            long contentLength,
            byte[] content) {

        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
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

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(
            long contentLength) {

        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(
            byte[] content) {

        this.content = content;
    }
}