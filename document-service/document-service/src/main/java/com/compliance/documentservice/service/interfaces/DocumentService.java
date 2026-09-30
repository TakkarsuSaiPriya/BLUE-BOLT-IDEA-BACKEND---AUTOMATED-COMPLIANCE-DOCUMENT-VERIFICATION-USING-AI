package com.compliance.documentservice.service.interfaces;

import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.request.DocumentUpdateRequest;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import com.compliance.documentservice.storage.DownloadedDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface DocumentService {

    DocumentUploadResponse uploadDocument(
            MultipartFile file,
            DocumentType documentType,
            String description,
            String username
    );

    DocumentResponse getDocumentById(
            Long documentId,
            String username,
            Set<String> roles
    );

    List<DocumentResponse> getAllDocuments(
            String username,
            Set<String> roles
    );

    List<DocumentResponse> getDocumentsByUser(
            String requestedUsername,
            String authenticatedUsername,
            Set<String> roles
    );

    List<DocumentResponse> getDocumentsByStatus(
            DocumentStatus status,
            String username,
            Set<String> roles
    );

    List<DocumentResponse> getDocumentsByType(
            DocumentType documentType,
            String username,
            Set<String> roles
    );

    DocumentResponse updateDocument(
            Long documentId,
            DocumentUpdateRequest request,
            String username,
            Set<String> roles
    );

    DocumentResponse updateDocumentStatus(
            Long documentId,
            DocumentStatusUpdateRequest request,
            String username,
            Set<String> roles
    );

    DownloadedDocument downloadDocument(
            Long documentId,
            String username,
            Set<String> roles
    );

    void deleteDocument(
            Long documentId,
            String username,
            Set<String> roles
    );
}