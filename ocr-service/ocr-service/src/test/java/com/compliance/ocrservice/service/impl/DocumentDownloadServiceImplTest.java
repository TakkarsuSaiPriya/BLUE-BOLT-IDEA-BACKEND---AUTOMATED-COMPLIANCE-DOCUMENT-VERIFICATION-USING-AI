package com.compliance.ocrservice.service.impl;

import com.compliance.ocrservice.client.DocumentServiceClient;
import com.compliance.ocrservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.ocrservice.exception.DocumentDownloadException;
import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.ocr.OcrDocumentContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadServiceImplTest {

    @Mock
    private DocumentServiceClient
            documentServiceClient;

    private DocumentDownloadServiceImpl
            documentDownloadService;

    @BeforeEach
    void setUp() {

        documentDownloadService =
                new DocumentDownloadServiceImpl(
                        documentServiceClient
                );
    }

    @Test
    void downloadDocumentShouldReturnDocumentContent() {

        byte[] documentBytes =
                "sample document"
                        .getBytes();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.add(
                "X-Document-Id",
                "10"
        );

        headers.add(
                "X-Document-Original-File-Name",
                "sample.pdf"
        );

        ResponseEntity<byte[]> response =
                new ResponseEntity<>(
                        documentBytes,
                        headers,
                        HttpStatus.OK
                );

        when(
                documentServiceClient
                        .downloadDocumentContent(
                                10L
                        )
        ).thenReturn(response);

        OcrDocumentContent result =
                documentDownloadService
                        .downloadDocument(
                                10L
                        );

        assertEquals(
                10L,
                result.getDocumentId()
        );

        assertEquals(
                "sample.pdf",
                result.getOriginalFileName()
        );

        assertEquals(
                "application/pdf",
                result.getContentType()
        );

        assertArrayEquals(
                documentBytes,
                result.getContent()
        );
    }

    @Test
    void downloadDocumentShouldUseRequestedIdWhenHeaderIsMissing() {

        byte[] documentBytes =
                "image"
                        .getBytes();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.IMAGE_PNG
        );

        headers.add(
                "X-Document-Original-File-Name",
                "sample.png"
        );

        when(
                documentServiceClient
                        .downloadDocumentContent(
                                5L
                        )
        ).thenReturn(
                new ResponseEntity<>(
                        documentBytes,
                        headers,
                        HttpStatus.OK
                )
        );

        OcrDocumentContent result =
                documentDownloadService
                        .downloadDocument(
                                5L
                        );

        assertEquals(
                5L,
                result.getDocumentId()
        );
    }

    @Test
    void downloadDocumentShouldRejectInvalidDocumentId() {

        assertThrows(
                InvalidOcrRequestException.class,
                () -> documentDownloadService
                        .downloadDocument(
                                0L
                        )
        );

        assertThrows(
                InvalidOcrRequestException.class,
                () -> documentDownloadService
                        .downloadDocument(
                                null
                        )
        );
    }

    @Test
    void downloadDocumentShouldRejectEmptyResponseBody() {

        when(
                documentServiceClient
                        .downloadDocumentContent(
                                20L
                        )
        ).thenReturn(
                ResponseEntity.ok(
                        new byte[0]
                )
        );

        assertThrows(
                DocumentDownloadException.class,
                () -> documentDownloadService
                        .downloadDocument(
                                20L
                        )
        );
    }

    @Test
    void downloadDocumentShouldRejectUnexpectedDocumentIdHeader() {

        byte[] content =
                "content".getBytes();

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "X-Document-Id",
                "999"
        );

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        when(
                documentServiceClient
                        .downloadDocumentContent(
                                10L
                        )
        ).thenReturn(
                new ResponseEntity<>(
                        content,
                        headers,
                        HttpStatus.OK
                )
        );

        assertThrows(
                DocumentDownloadException.class,
                () -> documentDownloadService
                        .downloadDocument(
                                10L
                        )
        );
    }

    @Test
    void markOcrProcessingShouldSendCorrectStatusRequest() {

        when(
                documentServiceClient
                        .updateDocumentStatus(
                                org.mockito.ArgumentMatchers.eq(
                                        12L
                                ),
                                org.mockito.ArgumentMatchers.any(
                                        DocumentStatusUpdateRequest.class
                                )
                        )
        ).thenReturn(
                ResponseEntity.noContent()
                        .build()
        );

        documentDownloadService
                .markOcrProcessing(
                        12L
                );

        ArgumentCaptor<DocumentStatusUpdateRequest>
                requestCaptor =
                ArgumentCaptor.forClass(
                        DocumentStatusUpdateRequest.class
                );

        verify(
                documentServiceClient
        ).updateDocumentStatus(
                org.mockito.ArgumentMatchers.eq(
                        12L
                ),
                requestCaptor.capture()
        );

        DocumentStatusUpdateRequest request =
                requestCaptor.getValue();

        assertEquals(
                "OCR_PROCESSING",
                request.getStatus()
        );

        assertEquals(
                "OCR text extraction started",
                request.getReason()
        );

        assertEquals(
                "ocr-service",
                request.getChangedBy()
        );
    }

    @Test
    void markOcrCompletedShouldUseDefaultReasonWhenBlank() {

        when(
                documentServiceClient
                        .updateDocumentStatus(
                                org.mockito.ArgumentMatchers.eq(
                                        15L
                                ),
                                org.mockito.ArgumentMatchers.any(
                                        DocumentStatusUpdateRequest.class
                                )
                        )
        ).thenReturn(
                ResponseEntity.noContent()
                        .build()
        );

        documentDownloadService
                .markOcrCompleted(
                        15L,
                        " "
                );

        ArgumentCaptor<DocumentStatusUpdateRequest>
                requestCaptor =
                ArgumentCaptor.forClass(
                        DocumentStatusUpdateRequest.class
                );

        verify(
                documentServiceClient
        ).updateDocumentStatus(
                org.mockito.ArgumentMatchers.eq(
                        15L
                ),
                requestCaptor.capture()
        );

        assertEquals(
                "OCR_COMPLETED",
                requestCaptor
                        .getValue()
                        .getStatus()
        );

        assertEquals(
                "OCR text extraction completed",
                requestCaptor
                        .getValue()
                        .getReason()
        );
    }

    @Test
    void markOcrFailedShouldLimitReasonToFiveHundredCharacters() {

        when(
                documentServiceClient
                        .updateDocumentStatus(
                                org.mockito.ArgumentMatchers.eq(
                                        18L
                                ),
                                org.mockito.ArgumentMatchers.any(
                                        DocumentStatusUpdateRequest.class
                                )
                        )
        ).thenReturn(
                ResponseEntity.noContent()
                        .build()
        );

        String longReason =
                "x".repeat(700);

        documentDownloadService
                .markOcrFailed(
                        18L,
                        longReason
                );

        ArgumentCaptor<DocumentStatusUpdateRequest>
                requestCaptor =
                ArgumentCaptor.forClass(
                        DocumentStatusUpdateRequest.class
                );

        verify(
                documentServiceClient
        ).updateDocumentStatus(
                org.mockito.ArgumentMatchers.eq(
                        18L
                ),
                requestCaptor.capture()
        );

        assertEquals(
                500,
                requestCaptor
                        .getValue()
                        .getReason()
                        .length()
        );
    }
}