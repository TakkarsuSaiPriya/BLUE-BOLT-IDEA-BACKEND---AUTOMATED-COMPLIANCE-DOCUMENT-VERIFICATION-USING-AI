package com.compliance.ocrservice.controller;

import com.compliance.ocrservice.dto.response.ApiResponse;
import com.compliance.ocrservice.dto.response.OcrResultResponse;
import com.compliance.ocrservice.dto.response.OcrSummaryResponse;
import com.compliance.ocrservice.enums.OcrStatus;
import com.compliance.ocrservice.service.interfaces.OcrService;
import com.compliance.ocrservice.util.SecurityContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrQueryControllerTest {

    @Mock
    private OcrService ocrService;

    @Mock
    private SecurityContextUtil
            securityContextUtil;

    @Mock
    private OcrResultResponse
            ocrResultResponse;

    @Mock
    private OcrSummaryResponse
            ocrSummaryResponse;

    private OcrQueryController
            ocrQueryController;

    @BeforeEach
    void setUp() {

        ocrQueryController =
                new OcrQueryController(
                        ocrService,
                        securityContextUtil
                );
    }

    @Test
    void getResultByIdShouldReturnServiceResult() {

        when(
                ocrService.getResultById(
                        10L
                )
        ).thenReturn(
                ocrResultResponse
        );

        ResponseEntity<ApiResponse<OcrResultResponse>>
                response =
                ocrQueryController.getResultById(
                        10L
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(
                response.getBody()
        );

        assertSame(
                ocrResultResponse,
                response
                        .getBody()
                        .getData()
        );

        verify(
                ocrService
        ).getResultById(
                10L
        );
    }

    @Test
    void getLatestResultShouldReturnServiceResult() {

        when(
                ocrService
                        .getLatestResultByDocumentId(
                                15L
                        )
        ).thenReturn(
                ocrResultResponse
        );

        ResponseEntity<ApiResponse<OcrResultResponse>>
                response =
                ocrQueryController
                        .getLatestResultByDocumentId(
                                15L
                        );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertSame(
                ocrResultResponse,
                response
                        .getBody()
                        .getData()
        );
    }

    @Test
    void getHistoryShouldReturnSummaryList() {

        when(
                ocrService
                        .getHistoryByDocumentId(
                                20L
                        )
        ).thenReturn(
                List.of(
                        ocrSummaryResponse
                )
        );

        ResponseEntity
                <ApiResponse<List<OcrSummaryResponse>>>
                response =
                ocrQueryController
                        .getHistoryByDocumentId(
                                20L
                        );

        assertEquals(
                1,
                response
                        .getBody()
                        .getData()
                        .size()
        );

        assertSame(
                ocrSummaryResponse,
                response
                        .getBody()
                        .getData()
                        .getFirst()
        );
    }

    @Test
    void getCurrentUserResultsShouldUseAuthenticatedUsername() {

        when(
                securityContextUtil
                        .getCurrentUsername()
        ).thenReturn(
                "testuser"
        );

        when(
                ocrService
                        .getResultsByRequestedUser(
                                "testuser"
                        )
        ).thenReturn(
                List.of(
                        ocrSummaryResponse
                )
        );

        ResponseEntity
                <ApiResponse<List<OcrSummaryResponse>>>
                response =
                ocrQueryController
                        .getCurrentUserResults();

        assertEquals(
                1,
                response
                        .getBody()
                        .getData()
                        .size()
        );

        verify(
                ocrService
        ).getResultsByRequestedUser(
                "testuser"
        );
    }

    @Test
    void getResultsByStatusShouldDelegateToService() {

        when(
                ocrService.getResultsByStatus(
                        OcrStatus.COMPLETED
                )
        ).thenReturn(
                List.of(
                        ocrSummaryResponse
                )
        );

        ResponseEntity
                <ApiResponse<List<OcrSummaryResponse>>>
                response =
                ocrQueryController
                        .getResultsByStatus(
                                OcrStatus.COMPLETED
                        );

        assertEquals(
                1,
                response
                        .getBody()
                        .getData()
                        .size()
        );

        verify(
                ocrService
        ).getResultsByStatus(
                OcrStatus.COMPLETED
        );
    }

    @Test
    void getStatusSummaryShouldReturnSummaryMap() {

        Map<String, Long> summary =
                new LinkedHashMap<>();

        summary.put(
                "TOTAL",
                10L
        );

        summary.put(
                "COMPLETED",
                8L
        );

        summary.put(
                "FAILED",
                2L
        );

        when(
                ocrService.getStatusSummary()
        ).thenReturn(summary);

        ResponseEntity
                <ApiResponse<Map<String, Long>>>
                response =
                ocrQueryController
                        .getStatusSummary();

        assertEquals(
                10L,
                response
                        .getBody()
                        .getData()
                        .get("TOTAL")
        );

        assertEquals(
                8L,
                response
                        .getBody()
                        .getData()
                        .get("COMPLETED")
        );
    }

    @Test
    void getAllResultsShouldReturnAllResults() {

        when(
                ocrService.getAllResults()
        ).thenReturn(
                List.of(
                        ocrSummaryResponse
                )
        );

        ResponseEntity
                <ApiResponse<List<OcrSummaryResponse>>>
                response =
                ocrQueryController
                        .getAllResults();

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertEquals(
                1,
                response
                        .getBody()
                        .getData()
                        .size()
        );
    }
}