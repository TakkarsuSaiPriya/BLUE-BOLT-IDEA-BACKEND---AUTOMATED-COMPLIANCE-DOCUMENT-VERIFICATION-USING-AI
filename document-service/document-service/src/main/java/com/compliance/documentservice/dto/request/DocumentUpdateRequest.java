package com.compliance.documentservice.dto.request;

import com.compliance.documentservice.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentUpdateRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    public DocumentUpdateRequest() {
    }

    public DocumentUpdateRequest(
            DocumentType documentType,
            String description) {

        this.documentType = documentType;
        this.description = description;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(
            DocumentType documentType) {

        this.documentType = documentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }
}