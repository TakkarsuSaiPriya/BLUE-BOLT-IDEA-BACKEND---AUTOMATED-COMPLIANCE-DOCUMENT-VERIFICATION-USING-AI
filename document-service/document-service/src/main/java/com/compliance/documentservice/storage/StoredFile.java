package com.compliance.documentservice.storage;

public class StoredFile {

    private String originalFileName;
    private String storedFileName;
    private String storagePath;
    private String fileExtension;
    private String contentType;
    private long fileSize;

    public StoredFile() {
    }

    public StoredFile(
            String originalFileName,
            String storedFileName,
            String storagePath,
            String fileExtension,
            String contentType,
            long fileSize) {

        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.fileExtension = fileExtension;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName) {

        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(
            String storedFileName) {

        this.storedFileName = storedFileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(
            String storagePath) {

        this.storagePath = storagePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(
            String fileExtension) {

        this.fileExtension = fileExtension;
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

    public void setFileSize(
            long fileSize) {

        this.fileSize = fileSize;
    }
}