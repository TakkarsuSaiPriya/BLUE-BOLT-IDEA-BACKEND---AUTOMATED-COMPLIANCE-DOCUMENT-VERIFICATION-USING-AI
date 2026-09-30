package com.compliance.documentservice.service.impl;

import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.request.DocumentUpdateRequest;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import com.compliance.documentservice.exception.DocumentNotFoundException;
import com.compliance.documentservice.exception.DuplicateDocumentException;
import com.compliance.documentservice.exception.FileStorageException;
import com.compliance.documentservice.exception.InvalidDocumentException;
import com.compliance.documentservice.exception.UnauthorizedDocumentAccessException;
import com.compliance.documentservice.mapper.DocumentMapper;
import com.compliance.documentservice.repository.DocumentRepository;
import com.compliance.documentservice.service.TransactionEventPublisher;
import com.compliance.documentservice.service.interfaces.DocumentService;
import com.compliance.documentservice.service.interfaces.FileStorageService;
import com.compliance.documentservice.storage.DownloadedDocument;
import com.compliance.documentservice.storage.StoredFile;
import com.compliance.documentservice.util.ChecksumUtil;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl
        implements DocumentService {

    private static final String ROLE_ADMIN =
            "ROLE_ADMIN";

    private static final String ROLE_AUDITOR =
            "ROLE_AUDITOR";

    private static final String ROLE_VERIFIER =
            "ROLE_VERIFIER";

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final ChecksumUtil checksumUtil;
    private final DocumentMapper documentMapper;
    private final TransactionEventPublisher eventPublisher;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            ChecksumUtil checksumUtil,
            DocumentMapper documentMapper,
            TransactionEventPublisher eventPublisher) {

        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.checksumUtil = checksumUtil;
        this.documentMapper = documentMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(
            MultipartFile file,
            DocumentType documentType,
            String description,
            String username) {

        validateAuthenticatedUsername(username);

        if (documentType == null) {
            throw new InvalidDocumentException(
                    "Document type is required"
            );
        }

        validateDescription(description);

        String normalizedUsername =
                normalizeUsername(username);

        String checksum =
                checksumUtil.calculateSha256(file);

        documentRepository
                .findByChecksumAndUploadedByAndActiveTrue(
                        checksum,
                        normalizedUsername
                )
                .ifPresent(existingDocument -> {
                    throw new DuplicateDocumentException(
                            file.getOriginalFilename(),
                            existingDocument.getId()
                    );
                });

        StoredFile storedFile =
                fileStorageService.store(file);

        try {
            Document document = new Document();

            document.setOriginalFileName(
                    storedFile.getOriginalFileName()
            );

            document.setStoredFileName(
                    storedFile.getStoredFileName()
            );

            document.setContentType(
                    storedFile.getContentType()
            );

            document.setFileExtension(
                    storedFile.getFileExtension()
            );

            document.setFileSize(
                    storedFile.getFileSize()
            );

            document.setStoragePath(
                    storedFile.getStoragePath()
            );

            document.setChecksum(checksum);
            document.setDocumentType(documentType);
            document.setStatus(DocumentStatus.UPLOADED);
            document.setUploadedBy(normalizedUsername);

            document.setDescription(
                    normalizeOptionalValue(description)
            );

            document.setStatusReason(null);
            document.setActive(true);

            Document savedDocument =
                    documentRepository.save(document);

            eventPublisher
                    .publishDocumentUploadedAfterCommit(
                            savedDocument
                    );

            return documentMapper.toUploadResponse(
                    savedDocument
            );

        } catch (RuntimeException exception) {

            deleteStoredFileQuietly(
                    storedFile.getStoredFileName()
            );

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(
            Long documentId,
            String username,
            Set<String> roles) {

        Document document =
                findActiveDocument(documentId);

        verifyReadAccess(
                document,
                username,
                roles
        );

        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments(
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        Set<String> normalizedRoles =
                normalizeRoles(roles);

        if (hasPrivilegedReadRole(normalizedRoles)) {

            return documentRepository
                    .findAllByActiveTrue()
                    .stream()
                    .map(documentMapper::toResponse)
                    .toList();
        }

        return documentRepository
                .findAllByUploadedByAndActiveTrue(
                        normalizeUsername(username)
                )
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByUser(
            String requestedUsername,
            String authenticatedUsername,
            Set<String> roles) {

        validateAuthenticatedUsername(requestedUsername);
        validateAuthenticatedUsername(authenticatedUsername);

        String requested =
                normalizeUsername(requestedUsername);

        String authenticated =
                normalizeUsername(authenticatedUsername);

        Set<String> normalizedRoles =
                normalizeRoles(roles);

        if (!requested.equals(authenticated)
                && !hasPrivilegedReadRole(normalizedRoles)) {

            throw new UnauthorizedDocumentAccessException(
                    "You are not authorized to view documents uploaded by another user"
            );
        }

        return documentRepository
                .findAllByUploadedByAndActiveTrue(requested)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByStatus(
            DocumentStatus status,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        if (status == null) {
            throw new InvalidDocumentException(
                    "Document status is required"
            );
        }

        Set<String> normalizedRoles =
                normalizeRoles(roles);

        if (!hasPrivilegedReadRole(normalizedRoles)) {
            throw new UnauthorizedDocumentAccessException(
                    "Only administrators, auditors, or verifiers can filter documents by status"
            );
        }

        return documentRepository
                .findAllByStatusAndActiveTrue(status)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByType(
            DocumentType documentType,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        if (documentType == null) {
            throw new InvalidDocumentException(
                    "Document type is required"
            );
        }

        Set<String> normalizedRoles =
                normalizeRoles(roles);

        if (hasPrivilegedReadRole(normalizedRoles)) {

            return documentRepository
                    .findAllByDocumentTypeAndActiveTrue(
                            documentType
                    )
                    .stream()
                    .map(documentMapper::toResponse)
                    .toList();
        }

        return documentRepository
                .findAllByUploadedByAndActiveTrue(
                        normalizeUsername(username)
                )
                .stream()
                .filter(document ->
                        document.getDocumentType()
                                == documentType)
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(
            Long documentId,
            DocumentUpdateRequest request,
            String username,
            Set<String> roles) {

        if (request == null) {
            throw new InvalidDocumentException(
                    "Document update request is required"
            );
        }

        if (request.getDocumentType() == null) {
            throw new InvalidDocumentException(
                    "Document type is required"
            );
        }

        validateDescription(request.getDescription());

        Document document =
                findActiveDocument(documentId);

        verifyOwnerOrAdminAccess(
                document,
                username,
                roles
        );

        document.setDocumentType(
                request.getDocumentType()
        );

        document.setDescription(
                normalizeOptionalValue(
                        request.getDescription()
                )
        );

        return documentMapper.toResponse(
                documentRepository.save(document)
        );
    }

    @Override
    @Transactional
    public DocumentResponse updateDocumentStatus(
            Long documentId,
            DocumentStatusUpdateRequest request,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        if (request == null) {
            throw new InvalidDocumentException(
                    "Document status update request is required"
            );
        }

        if (request.getStatus() == null) {
            throw new InvalidDocumentException(
                    "Document status is required"
            );
        }

        validateStatusReason(request.getReason());

        if (!canUpdateStatus(normalizeRoles(roles))) {
            throw new UnauthorizedDocumentAccessException(
                    "Only administrators or verifiers can update document status"
            );
        }

        Document document =
                findActiveDocument(documentId);

        DocumentStatus previousStatus =
                document.getStatus();

        validateStatusTransition(
                previousStatus,
                request.getStatus()
        );

        document.setStatus(request.getStatus());

        document.setStatusReason(
                normalizeOptionalValue(
                        request.getReason()
                )
        );

        Document savedDocument =
                documentRepository.save(document);

        eventPublisher.publishStatusChangedAfterCommit(
                savedDocument,
                previousStatus,
                normalizeUsername(username)
        );

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedDocument downloadDocument(
            Long documentId,
            String username,
            Set<String> roles) {

        Document document =
                findActiveDocument(documentId);

        verifyReadAccess(
                document,
                username,
                roles
        );

        Resource resource =
                fileStorageService.loadAsResource(
                        document.getStoredFileName()
                );

        return new DownloadedDocument(
                resource,
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize()
        );
    }

    @Override
    @Transactional
    public void deleteDocument(
            Long documentId,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        if (!normalizeRoles(roles).contains(ROLE_ADMIN)) {
            throw new UnauthorizedDocumentAccessException(
                    "Only administrators can delete documents"
            );
        }

        Document document =
                findActiveDocument(documentId);

        fileStorageService.delete(
                document.getStoredFileName()
        );

        document.setActive(false);
        document.setStatus(DocumentStatus.REJECTED);

        document.setStatusReason(
                "Document deleted by administrator"
        );

        Document savedDocument =
                documentRepository.save(document);

        eventPublisher.publishDocumentDeletedAfterCommit(
                savedDocument,
                normalizeUsername(username)
        );
    }

    private Document findActiveDocument(
            Long documentId) {

        if (documentId == null || documentId <= 0) {
            throw new InvalidDocumentException(
                    "Document ID must be a positive number"
            );
        }

        return documentRepository
                .findByIdAndActiveTrue(documentId)
                .orElseThrow(() ->
                        new DocumentNotFoundException(
                                documentId
                        )
                );
    }

    private void verifyReadAccess(
            Document document,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        String normalizedUsername =
                normalizeUsername(username);

        boolean ownsDocument =
                document.getUploadedBy()
                        .equalsIgnoreCase(
                                normalizedUsername
                        );

        if (!ownsDocument
                && !hasPrivilegedReadRole(
                normalizeRoles(roles))) {

            throw new UnauthorizedDocumentAccessException(
                    document.getId(),
                    normalizedUsername
            );
        }
    }

    private void verifyOwnerOrAdminAccess(
            Document document,
            String username,
            Set<String> roles) {

        validateAuthenticatedUsername(username);

        String normalizedUsername =
                normalizeUsername(username);

        boolean ownsDocument =
                document.getUploadedBy()
                        .equalsIgnoreCase(
                                normalizedUsername
                        );

        boolean administrator =
                normalizeRoles(roles)
                        .contains(ROLE_ADMIN);

        if (!ownsDocument && !administrator) {
            throw new UnauthorizedDocumentAccessException(
                    document.getId(),
                    normalizedUsername
            );
        }
    }

    private boolean hasPrivilegedReadRole(
            Set<String> roles) {

        return roles.contains(ROLE_ADMIN)
                || roles.contains(ROLE_AUDITOR)
                || roles.contains(ROLE_VERIFIER);
    }

    private boolean canUpdateStatus(
            Set<String> roles) {

        return roles.contains(ROLE_ADMIN)
                || roles.contains(ROLE_VERIFIER);
    }

    private Set<String> normalizeRoles(
            Set<String> roles) {

        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .filter(role ->
                        role != null
                                && !role.isBlank())
                .map(role ->
                        role.trim()
                                .toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private void validateStatusTransition(
            DocumentStatus currentStatus,
            DocumentStatus requestedStatus) {

        if (currentStatus == requestedStatus) {
            throw new InvalidDocumentException(
                    "Document already has status: "
                            + requestedStatus
            );
        }

        if (currentStatus == DocumentStatus.APPROVED
                && requestedStatus != DocumentStatus.REJECTED) {

            throw new InvalidDocumentException(
                    "An approved document can only be changed to REJECTED"
            );
        }

        if (currentStatus == DocumentStatus.REJECTED
                && requestedStatus != DocumentStatus.UPLOADED) {

            throw new InvalidDocumentException(
                    "A rejected document can only be returned to UPLOADED status"
            );
        }
    }

    private void validateAuthenticatedUsername(
            String username) {

        if (username == null || username.isBlank()) {
            throw new UnauthorizedDocumentAccessException(
                    "Authenticated username is required"
            );
        }
    }

    private String normalizeUsername(
            String username) {

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateDescription(
            String description) {

        if (description != null
                && description.length() > 500) {

            throw new InvalidDocumentException(
                    "Description cannot exceed 500 characters"
            );
        }
    }

    private void validateStatusReason(
            String reason) {

        if (reason != null
                && reason.length() > 500) {

            throw new InvalidDocumentException(
                    "Status reason cannot exceed 500 characters"
            );
        }
    }

    private String normalizeOptionalValue(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void deleteStoredFileQuietly(
            String storedFileName) {

        try {
            fileStorageService.delete(storedFileName);

        } catch (FileStorageException exception) {
            // Original database exception must be preserved.
        }
    }
}