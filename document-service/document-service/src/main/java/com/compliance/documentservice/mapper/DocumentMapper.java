package com.compliance.documentservice.mapper;

import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(
            Document document) {

        if (document == null) {
            return null;
        }

        DocumentResponse response =
                new DocumentResponse();

        response.setId(document.getId());

        response.setOriginalFileName(
                document.getOriginalFileName()
        );

        response.setContentType(
                document.getContentType()
        );

        response.setFileExtension(
                document.getFileExtension()
        );

        response.setFileSize(
                document.getFileSize()
        );

        response.setChecksum(
                document.getChecksum()
        );

        response.setDocumentType(
                document.getDocumentType()
        );

        response.setStatus(
                document.getStatus()
        );

        response.setUploadedBy(
                document.getUploadedBy()
        );

        response.setDescription(
                document.getDescription()
        );

        response.setStatusReason(
                document.getStatusReason()
        );

        response.setActive(
                document.isActive()
        );

        response.setCreatedAt(
                document.getCreatedAt()
        );

        response.setUpdatedAt(
                document.getUpdatedAt()
        );

        return response;
    }

    public DocumentUploadResponse toUploadResponse(
            Document document) {

        if (document == null) {
            return null;
        }

        DocumentUploadResponse response =
                new DocumentUploadResponse();

        response.setDocumentId(
                document.getId()
        );

        response.setOriginalFileName(
                document.getOriginalFileName()
        );

        response.setDocumentType(
                document.getDocumentType()
        );

        response.setStatus(
                document.getStatus()
        );

        response.setUploadedBy(
                document.getUploadedBy()
        );

        response.setMessage(
                "Document uploaded successfully"
        );

        response.setUploadedAt(
                document.getCreatedAt()
        );

        return response;
    }
}