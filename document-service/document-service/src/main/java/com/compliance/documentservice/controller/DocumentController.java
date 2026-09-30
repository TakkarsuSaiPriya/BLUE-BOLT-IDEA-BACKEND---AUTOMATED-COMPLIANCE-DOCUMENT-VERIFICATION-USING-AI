package com.compliance.documentservice.controller;

import com.compliance.documentservice.audit.Auditable;
import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.request.DocumentUpdateRequest;
import com.compliance.documentservice.dto.response.ApiResponse;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.enums.AuditActionType;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import com.compliance.documentservice.service.interfaces.DocumentService;
import com.compliance.documentservice.storage.DownloadedDocument;
import com.compliance.documentservice.util.SecurityContextUtil;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityContextUtil securityContextUtil;

    public DocumentController(
            DocumentService documentService,
            SecurityContextUtil securityContextUtil) {

        this.documentService = documentService;
        this.securityContextUtil = securityContextUtil;
    }

    @Auditable(action = AuditActionType.DOCUMENT_UPLOADED)
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<DocumentUploadResponse>>
    uploadDocument(
            @RequestPart("file")
            MultipartFile file,
            @RequestParam("documentType")
            DocumentType documentType,
            @RequestParam(
                    value = "description",
                    required = false
            )
            String description) {

        DocumentUploadResponse response =
                documentService.uploadDocument(
                        file,
                        documentType,
                        description,
                        securityContextUtil.getCurrentUsername()
                );

        return ResponseEntity.status(201)
                .body(
                        new ApiResponse<>(
                                true,
                                "Document uploaded successfully",
                                response
                        )
                );
    }

    @Auditable(action = AuditActionType.DOCUMENT_VIEWED)
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<DocumentResponse>>
    getDocumentById(
            @PathVariable("id")
            Long documentId) {

        DocumentResponse response =
                documentService.getDocumentById(
                        documentId,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document fetched successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_LIST_VIEWED
    )
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<List<DocumentResponse>>>
    getAllDocuments() {

        List<DocumentResponse> response =
                documentService.getAllDocuments(
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.USER_DOCUMENTS_VIEWED
    )
    @GetMapping("/user/{username}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<List<DocumentResponse>>>
    getDocumentsByUser(
            @PathVariable("username")
            String requestedUsername) {

        List<DocumentResponse> response =
                documentService.getDocumentsByUser(
                        requestedUsername,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User documents fetched successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_LIST_VIEWED
    )
    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<List<DocumentResponse>>>
    getDocumentsByStatus(
            @PathVariable("status")
            DocumentStatus status) {

        List<DocumentResponse> response =
                documentService.getDocumentsByStatus(
                        status,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched by status successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_LIST_VIEWED
    )
    @GetMapping("/type/{documentType}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<List<DocumentResponse>>>
    getDocumentsByType(
            @PathVariable("documentType")
            DocumentType documentType) {

        List<DocumentResponse> response =
                documentService.getDocumentsByType(
                        documentType,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched by type successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_DOWNLOADED
    )
    @GetMapping("/{id}/download")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable("id")
            Long documentId) {

        DownloadedDocument document =
                documentService.downloadDocument(
                        documentId,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        MediaType mediaType =
                resolveMediaType(
                        document.getContentType()
                );

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                document.getOriginalFileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(document.getFileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(document.getResource());
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_UPDATED
    )
    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    public ResponseEntity<ApiResponse<DocumentResponse>>
    updateDocument(
            @PathVariable("id")
            Long documentId,
            @Valid
            @RequestBody
            DocumentUpdateRequest request) {

        DocumentResponse response =
                documentService.updateDocument(
                        documentId,
                        request,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document metadata updated successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_STATUS_CHANGED
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'VERIFIER')"
    )
    public ResponseEntity<ApiResponse<DocumentResponse>>
    updateDocumentStatus(
            @PathVariable("id")
            Long documentId,
            @Valid
            @RequestBody
            DocumentStatusUpdateRequest request) {

        DocumentResponse response =
                documentService.updateDocumentStatus(
                        documentId,
                        request,
                        securityContextUtil.getCurrentUsername(),
                        securityContextUtil.getCurrentRoles()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document status updated successfully",
                        response
                )
        );
    }

    @Auditable(
            action = AuditActionType.DOCUMENT_DELETED
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>>
    deleteDocument(
            @PathVariable("id")
            Long documentId) {

        documentService.deleteDocument(
                documentId,
                securityContextUtil.getCurrentUsername(),
                securityContextUtil.getCurrentRoles()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document deleted successfully",
                        "Deleted"
                )
        );
    }

    private MediaType resolveMediaType(
            String contentType) {

        if (contentType == null
                || contentType.isBlank()) {

            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);

        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}