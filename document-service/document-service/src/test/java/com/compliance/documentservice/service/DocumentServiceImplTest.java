package com.compliance.documentservice.service;

import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.request.DocumentUpdateRequest;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import com.compliance.documentservice.exception.DocumentNotFoundException;
import com.compliance.documentservice.exception.DuplicateDocumentException;
import com.compliance.documentservice.exception.UnauthorizedDocumentAccessException;
import com.compliance.documentservice.mapper.DocumentMapper;
import com.compliance.documentservice.repository.DocumentRepository;
import com.compliance.documentservice.service.impl.DocumentServiceImpl;
import com.compliance.documentservice.service.interfaces.FileStorageService;
import com.compliance.documentservice.storage.StoredFile;
import com.compliance.documentservice.util.ChecksumUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ChecksumUtil checksumUtil;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private TransactionEventPublisher eventPublisher;

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {

        documentService =
                new DocumentServiceImpl(
                        documentRepository,
                        fileStorageService,
                        checksumUtil,
                        documentMapper,
                        eventPublisher
                );
    }

    @Test
    void uploadDocumentShouldStoreMetadataAndPublishEvent() {

        MockMultipartFile file =
                createPdfFile();

        StoredFile storedFile =
                new StoredFile(
                        "pan-card.pdf",
                        "stored-document.pdf",
                        "uploads/stored-document.pdf",
                        "pdf",
                        "application/pdf",
                        file.getSize()
                );

        Document savedDocument =
                createDocument();

        DocumentUploadResponse expectedResponse =
                new DocumentUploadResponse(
                        1L,
                        "pan-card.pdf",
                        DocumentType.PAN_CARD,
                        DocumentStatus.UPLOADED,
                        "saipriya",
                        "Document uploaded successfully",
                        LocalDateTime.now()
                );

        when(checksumUtil.calculateSha256(file))
                .thenReturn("checksum-value");

        when(documentRepository
                .findByChecksumAndUploadedByAndActiveTrue(
                        "checksum-value",
                        "saipriya"
                ))
                .thenReturn(Optional.empty());

        when(fileStorageService.store(file))
                .thenReturn(storedFile);

        when(documentRepository.save(
                any(Document.class)))
                .thenReturn(savedDocument);

        when(documentMapper.toUploadResponse(
                savedDocument))
                .thenReturn(expectedResponse);

        DocumentUploadResponse result =
                documentService.uploadDocument(
                        file,
                        DocumentType.PAN_CARD,
                        "Employee PAN card",
                        "SaiPriya"
                );

        assertNotNull(result);
        assertEquals(1L, result.getDocumentId());
        assertEquals(
                DocumentStatus.UPLOADED,
                result.getStatus()
        );

        ArgumentCaptor<Document> documentCaptor =
                ArgumentCaptor.forClass(
                        Document.class
                );

        verify(documentRepository)
                .save(documentCaptor.capture());

        Document capturedDocument =
                documentCaptor.getValue();

        assertEquals(
                "saipriya",
                capturedDocument.getUploadedBy()
        );
        assertEquals(
                "checksum-value",
                capturedDocument.getChecksum()
        );
        assertEquals(
                DocumentType.PAN_CARD,
                capturedDocument.getDocumentType()
        );

        verify(eventPublisher)
                .publishDocumentUploadedAfterCommit(
                        savedDocument
                );
    }

    @Test
    void uploadDocumentShouldRejectDuplicate() {

        MockMultipartFile file =
                createPdfFile();

        Document existingDocument =
                createDocument();

        when(checksumUtil.calculateSha256(file))
                .thenReturn("checksum-value");

        when(documentRepository
                .findByChecksumAndUploadedByAndActiveTrue(
                        "checksum-value",
                        "saipriya"
                ))
                .thenReturn(
                        Optional.of(existingDocument)
                );

        assertThrows(
                DuplicateDocumentException.class,
                () -> documentService.uploadDocument(
                        file,
                        DocumentType.PAN_CARD,
                        "Employee PAN card",
                        "saipriya"
                )
        );

        verify(fileStorageService, never())
                .store(file);

        verify(documentRepository, never())
                .save(any(Document.class));
    }

    @Test
    void getDocumentByIdShouldReturnOwnerDocument() {

        Document document =
                createDocument();

        DocumentResponse expectedResponse =
                createDocumentResponse();

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        when(documentMapper.toResponse(document))
                .thenReturn(expectedResponse);

        DocumentResponse result =
                documentService.getDocumentById(
                        1L,
                        "saipriya",
                        Set.of("ROLE_USER")
                );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                "saipriya",
                result.getUploadedBy()
        );
    }

    @Test
    void getDocumentByIdShouldRejectAnotherUser() {

        Document document =
                createDocument();

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        assertThrows(
                UnauthorizedDocumentAccessException.class,
                () -> documentService.getDocumentById(
                        1L,
                        "anotheruser",
                        Set.of("ROLE_USER")
                )
        );
    }

    @Test
    void getDocumentByIdShouldAllowAdministrator() {

        Document document =
                createDocument();

        DocumentResponse expectedResponse =
                createDocumentResponse();

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        when(documentMapper.toResponse(document))
                .thenReturn(expectedResponse);

        DocumentResponse result =
                documentService.getDocumentById(
                        1L,
                        "administrator",
                        Set.of("ROLE_ADMIN")
                );

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getDocumentByIdShouldThrowWhenMissing() {

        when(documentRepository
                .findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        DocumentNotFoundException exception =
                assertThrows(
                        DocumentNotFoundException.class,
                        () -> documentService.getDocumentById(
                                999L,
                                "saipriya",
                                Set.of("ROLE_USER")
                        )
                );

        assertEquals(
                "Document not found with ID: 999",
                exception.getMessage()
        );
    }

    @Test
    void getAllDocumentsShouldReturnOnlyUserDocuments() {

        Document document =
                createDocument();

        DocumentResponse response =
                createDocumentResponse();

        when(documentRepository
                .findAllByUploadedByAndActiveTrue(
                        "saipriya"
                ))
                .thenReturn(List.of(document));

        when(documentMapper.toResponse(document))
                .thenReturn(response);

        List<DocumentResponse> result =
                documentService.getAllDocuments(
                        "saipriya",
                        Set.of("ROLE_USER")
                );

        assertEquals(1, result.size());

        verify(documentRepository)
                .findAllByUploadedByAndActiveTrue(
                        "saipriya"
                );

        verify(documentRepository, never())
                .findAllByActiveTrue();
    }

    @Test
    void getAllDocumentsShouldReturnAllForAdministrator() {

        Document document =
                createDocument();

        when(documentRepository.findAllByActiveTrue())
                .thenReturn(List.of(document));

        when(documentMapper.toResponse(document))
                .thenReturn(createDocumentResponse());

        List<DocumentResponse> result =
                documentService.getAllDocuments(
                        "administrator",
                        Set.of("ROLE_ADMIN")
                );

        assertEquals(1, result.size());

        verify(documentRepository)
                .findAllByActiveTrue();
    }

    @Test
    void updateDocumentStatusShouldPublishEvent() {

        Document document =
                createDocument();

        DocumentStatusUpdateRequest request =
                new DocumentStatusUpdateRequest(
                        DocumentStatus.QUEUED_FOR_OCR,
                        "Queued for processing"
                );

        DocumentResponse response =
                createDocumentResponse();

        response.setStatus(
                DocumentStatus.QUEUED_FOR_OCR
        );

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        when(documentMapper.toResponse(document))
                .thenReturn(response);

        DocumentResponse result =
                documentService.updateDocumentStatus(
                        1L,
                        request,
                        "verifier",
                        Set.of("ROLE_VERIFIER")
                );

        assertEquals(
                DocumentStatus.QUEUED_FOR_OCR,
                result.getStatus()
        );

        verify(eventPublisher)
                .publishStatusChangedAfterCommit(
                        document,
                        DocumentStatus.UPLOADED,
                        "verifier"
                );
    }

    @Test
    void updateDocumentStatusShouldRejectUserRole() {

        DocumentStatusUpdateRequest request =
                new DocumentStatusUpdateRequest(
                        DocumentStatus.QUEUED_FOR_OCR,
                        "Queued"
                );

        assertThrows(
                UnauthorizedDocumentAccessException.class,
                () -> documentService.updateDocumentStatus(
                        1L,
                        request,
                        "saipriya",
                        Set.of("ROLE_USER")
                )
        );

        verify(documentRepository, never())
                .save(any(Document.class));
    }

    @Test
    void updateDocumentShouldAllowOwner() {

        Document document =
                createDocument();

        DocumentUpdateRequest request =
                new DocumentUpdateRequest(
                        DocumentType.PASSPORT,
                        "Updated document"
                );

        DocumentResponse response =
                createDocumentResponse();

        response.setDocumentType(
                DocumentType.PASSPORT
        );

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        when(documentMapper.toResponse(document))
                .thenReturn(response);

        DocumentResponse result =
                documentService.updateDocument(
                        1L,
                        request,
                        "saipriya",
                        Set.of("ROLE_USER")
                );

        assertEquals(
                DocumentType.PASSPORT,
                result.getDocumentType()
        );

        verify(documentRepository).save(document);
    }

    @Test
    void deleteDocumentShouldSoftDeleteAndPublishEvent() {

        Document document =
                createDocument();

        when(documentRepository
                .findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        documentService.deleteDocument(
                1L,
                "administrator",
                Set.of("ROLE_ADMIN")
        );

        assertFalse(document.isActive());

        assertEquals(
                DocumentStatus.REJECTED,
                document.getStatus()
        );

        verify(fileStorageService)
                .delete("stored-document.pdf");

        verify(documentRepository)
                .save(document);

        verify(eventPublisher)
                .publishDocumentDeletedAfterCommit(
                        document,
                        "administrator"
                );
    }

    @Test
    void deleteDocumentShouldRejectNonAdministrator() {

        assertThrows(
                UnauthorizedDocumentAccessException.class,
                () -> documentService.deleteDocument(
                        1L,
                        "saipriya",
                        Set.of("ROLE_USER")
                )
        );

        verify(documentRepository, never())
                .findByIdAndActiveTrue(any());
    }

    private MockMultipartFile createPdfFile() {

        return new MockMultipartFile(
                "file",
                "pan-card.pdf",
                "application/pdf",
                new byte[]{
                        0x25,
                        0x50,
                        0x44,
                        0x46,
                        0x2D
                }
        );
    }

    private Document createDocument() {

        LocalDateTime now =
                LocalDateTime.now();

        Document document = new Document();

        document.setId(1L);
        document.setOriginalFileName("pan-card.pdf");
        document.setStoredFileName(
                "stored-document.pdf"
        );
        document.setContentType("application/pdf");
        document.setFileExtension("pdf");
        document.setFileSize(1024L);
        document.setStoragePath(
                "uploads/stored-document.pdf"
        );
        document.setChecksum("checksum-value");
        document.setDocumentType(DocumentType.PAN_CARD);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedBy("saipriya");
        document.setDescription("Employee PAN card");
        document.setActive(true);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        return document;
    }

    private DocumentResponse createDocumentResponse() {

        DocumentResponse response =
                new DocumentResponse();

        response.setId(1L);
        response.setOriginalFileName("pan-card.pdf");
        response.setContentType("application/pdf");
        response.setFileExtension("pdf");
        response.setFileSize(1024L);
        response.setChecksum("checksum-value");
        response.setDocumentType(DocumentType.PAN_CARD);
        response.setStatus(DocumentStatus.UPLOADED);
        response.setUploadedBy("saipriya");
        response.setDescription("Employee PAN card");
        response.setActive(true);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        return response;
    }
}