package com.compliance.documentservice.controller;

import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import com.compliance.documentservice.service.interfaces.DocumentService;
import com.compliance.documentservice.util.SecurityContextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private SecurityContextUtil securityContextUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        DocumentController documentController =
                new DocumentController(
                        documentService,
                        securityContextUtil
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(documentController)
                .build();

        objectMapper = new ObjectMapper();

        objectMapper.findAndRegisterModules();
    }

    @Test
    void uploadDocumentShouldReturnCreated()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
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

        DocumentUploadResponse response =
                new DocumentUploadResponse(
                        1L,
                        "pan-card.pdf",
                        DocumentType.PAN_CARD,
                        DocumentStatus.UPLOADED,
                        "saipriya",
                        "Document uploaded successfully",
                        LocalDateTime.now()
                );

        when(securityContextUtil.getCurrentUsername())
                .thenReturn("saipriya");

        when(documentService.uploadDocument(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(
                        DocumentType.PAN_CARD
                ),
                ArgumentMatchers.eq(
                        "Employee PAN card"
                ),
                ArgumentMatchers.eq("saipriya")
        )).thenReturn(response);

        mockMvc.perform(
                        multipart(
                                "/api/documents/upload"
                        )
                                .file(file)
                                .param(
                                        "documentType",
                                        "PAN_CARD"
                                )
                                .param(
                                        "description",
                                        "Employee PAN card"
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Document uploaded successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.documentId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("UPLOADED")
                );

        verify(documentService)
                .uploadDocument(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(
                                DocumentType.PAN_CARD
                        ),
                        ArgumentMatchers.eq(
                                "Employee PAN card"
                        ),
                        ArgumentMatchers.eq(
                                "saipriya"
                        )
                );
    }

    @Test
    void getDocumentByIdShouldReturnDocument()
            throws Exception {

        DocumentResponse response =
                createDocumentResponse();

        when(securityContextUtil.getCurrentUsername())
                .thenReturn("saipriya");

        when(securityContextUtil.getCurrentRoles())
                .thenReturn(
                        Set.of("ROLE_USER")
                );

        when(documentService.getDocumentById(
                1L,
                "saipriya",
                Set.of("ROLE_USER")
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/documents/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.uploadedBy")
                                .value("saipriya")
                )
                .andExpect(
                        jsonPath("$.data.documentType")
                                .value("PAN_CARD")
                );
    }

    @Test
    void getAllDocumentsShouldReturnList()
            throws Exception {

        when(securityContextUtil.getCurrentUsername())
                .thenReturn("saipriya");

        when(securityContextUtil.getCurrentRoles())
                .thenReturn(
                        Set.of("ROLE_USER")
                );

        when(documentService.getAllDocuments(
                "saipriya",
                Set.of("ROLE_USER")
        )).thenReturn(
                List.of(createDocumentResponse())
        );

        mockMvc.perform(
                        get("/api/documents")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data[0].status")
                                .value("UPLOADED")
                );
    }

    @Test
    void updateDocumentStatusShouldReturnUpdatedDocument()
            throws Exception {

        DocumentStatusUpdateRequest request =
                new DocumentStatusUpdateRequest(
                        DocumentStatus.QUEUED_FOR_OCR,
                        "Queued for OCR"
                );

        DocumentResponse response =
                createDocumentResponse();

        response.setStatus(
                DocumentStatus.QUEUED_FOR_OCR
        );

        response.setStatusReason(
                "Queued for OCR"
        );

        when(securityContextUtil.getCurrentUsername())
                .thenReturn("verifier");

        when(securityContextUtil.getCurrentRoles())
                .thenReturn(
                        Set.of("ROLE_VERIFIER")
                );

        when(documentService.updateDocumentStatus(
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.any(
                        DocumentStatusUpdateRequest.class
                ),
                ArgumentMatchers.eq("verifier"),
                ArgumentMatchers.eq(
                        Set.of("ROLE_VERIFIER")
                )
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/documents/1/status"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value(
                                        "QUEUED_FOR_OCR"
                                )
                )
                .andExpect(
                        jsonPath("$.data.statusReason")
                                .value(
                                        "Queued for OCR"
                                )
                );
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