package com.compliance.documentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Component
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String uploadDirectory = "uploads";

    private long maximumFileSize = 10485760L;

    private List<String> allowedContentTypes =
            new ArrayList<>();

    private List<String> allowedExtensions =
            new ArrayList<>();

    public StorageProperties() {
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(
            String uploadDirectory) {

        this.uploadDirectory = uploadDirectory;
    }

    public long getMaximumFileSize() {
        return maximumFileSize;
    }

    public void setMaximumFileSize(
            long maximumFileSize) {

        this.maximumFileSize = maximumFileSize;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(
            List<String> allowedContentTypes) {

        this.allowedContentTypes =
                allowedContentTypes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(
            List<String> allowedExtensions) {

        this.allowedExtensions =
                allowedExtensions;
    }
}