package com.compliance.documentservice.storage;

import org.springframework.core.io.Resource;

public class DownloadedDocument {

    private Resource resource;
    private String originalFileName;
    private String contentType;
    private long fileSize;

    public DownloadedDocument() {
    }

    public DownloadedDocument(
            Resource resource,
            String originalFileName,
            String contentType,
            long fileSize) {

        this.resource = resource;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}