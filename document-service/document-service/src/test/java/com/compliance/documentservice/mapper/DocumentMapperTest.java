package com.compliance.documentservice.mapper;

import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentMapperTest {

    private DocumentMapper documentMapper;

    @BeforeEach
    void setUp() {
        documentMapper = new DocumentMapper();
    }

    @Test
    void toResponseShouldMapDocument() {

        Document document = createDocument();

        DocumentResponse response =
                documentMapper.toResponse(document);

        assertEquals(1L, response.getId());
        assertEquals(
                "pan-card.pdf",
                response.getOriginalFileName()
        );
        assertEquals(
                "application/pdf",
                response.getContentType()
        );
        assertEquals("pdf", response.getFileExtension());
        assertEquals(1024L, response.getFileSize());
        assertEquals("checksum-value", response.getChecksum());
        assertEquals(
                DocumentType.PAN_CARD,
                response.getDocumentType()
        );
        assertEquals(
                DocumentStatus.UPLOADED,
                response.getStatus()
        );
        assertEquals("saipriya", response.getUploadedBy());
        assertEquals(
                "Employee PAN card",
                response.getDescription()
        );
        assertEquals(
                document.getCreatedAt(),
                response.getCreatedAt()
        );
        assertEquals(
                document.getUpdatedAt(),
                response.getUpdatedAt()
        );
    }

    @Test
    void toUploadResponseShouldMapDocument() {

        Document document = createDocument();

        DocumentUploadResponse response =
                documentMapper.toUploadResponse(document);

        assertEquals(1L, response.getDocumentId());

        assertEquals(
                "pan-card.pdf",
                response.getOriginalFileName()
        );

        assertEquals(
                DocumentType.PAN_CARD,
                response.getDocumentType()
        );

        assertEquals(
                DocumentStatus.UPLOADED,
                response.getStatus()
        );

        assertEquals(
                "saipriya",
                response.getUploadedBy()
        );

        assertEquals(
                "Document uploaded successfully",
                response.getMessage()
        );

        assertEquals(
                document.getCreatedAt(),
                response.getUploadedAt()
        );
    }

    @Test
    void toResponseShouldReturnNullForNullDocument() {

        DocumentResponse response =
                documentMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    void toUploadResponseShouldReturnNullForNullDocument() {

        DocumentUploadResponse response =
                documentMapper.toUploadResponse(null);

        assertNull(response);
    }

    private Document createDocument() {

        LocalDateTime now =
                LocalDateTime.now();

        Document document = new Document();

        document.setId(1L);
        document.setOriginalFileName("pan-card.pdf");
        document.setStoredFileName("stored-file.pdf");
        document.setContentType("application/pdf");
        document.setFileExtension("pdf");
        document.setFileSize(1024L);
        document.setStoragePath(
                "uploads/stored-file.pdf"
        );
        document.setChecksum("checksum-value");
        document.setDocumentType(DocumentType.PAN_CARD);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedBy("saipriya");
        document.setDescription("Employee PAN card");
        document.setStatusReason(null);
        document.setActive(true);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        return document;
    }
}