package com.compliance.verificationservice.controller;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.request.ReviewDecisionRequest;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.dto.response.VerificationSummaryResponse;
import com.compliance.verificationservice.enums.ReviewDecision;
import com.compliance.verificationservice.enums.VerificationStatus;
import com.compliance.verificationservice.exception.GlobalExceptionHandler;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.exception.VerificationNotFoundException;
import com.compliance.verificationservice.service.interfaces.OcrResultService;
import com.compliance.verificationservice.service.interfaces.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VerificationControllerTest {

    @Mock
    private VerificationService verificationService;

    @Mock
    private OcrResultService ocrResultService;

    private MockMvc mockMvc;

    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {

        VerificationController controller =
                new VerificationController(
                        verificationService,
                        ocrResultService
                );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .setValidator(validator)
                        .build();

        authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        null,
                        List.of()
                );
    }

    @Test
    @DisplayName(
            "Should manually process verification"
    )
    void shouldProcessManualVerification()
            throws Exception {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        VerificationResultResponse response =
                createVerificationResponse();

        when(
                ocrResultService.getOcrResult(2L)
        ).thenReturn(ocrResult);

        when(
                verificationService.processVerification(
                        eq(ocrResult),
                        eq("GENERAL"),
                        eq("testuser"),
                        eq(false)
                )
        ).thenReturn(response);

        String requestBody =
                """
                {
                  "ocrResultId": 2,
                  "documentType": "general",
                  "forceReprocess": false
                }
                """;

        mockMvc.perform(
                        post("/api/verifications/manual")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification completed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.data.documentId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.ocrResultId")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("VERIFIED")
                )
                .andExpect(
                        jsonPath("$.data.verificationScore")
                                .value(95.0)
                );

        verify(
                ocrResultService
        ).getOcrResult(2L);

        verify(
                verificationService
        ).processVerification(
                ocrResult,
                "GENERAL",
                "testuser",
                false
        );
    }

    @Test
    @DisplayName(
            "Should process forced manual verification"
    )
    void shouldProcessForcedManualVerification()
            throws Exception {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        VerificationResultResponse response =
                createVerificationResponse();

        when(
                ocrResultService.getOcrResult(2L)
        ).thenReturn(ocrResult);

        when(
                verificationService.processVerification(
                        eq(ocrResult),
                        eq("IDENTITY_DOCUMENT"),
                        eq("testuser"),
                        eq(true)
                )
        ).thenReturn(response);

        String requestBody =
                """
                {
                  "ocrResultId": 2,
                  "documentType": "identity_document",
                  "forceReprocess": true
                }
                """;

        mockMvc.perform(
                        post("/api/verifications/manual")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(10)
                );

        verify(
                verificationService
        ).processVerification(
                ocrResult,
                "IDENTITY_DOCUMENT",
                "testuser",
                true
        );
    }

    @Test
    @DisplayName(
            "Should reject manual request without OCR result ID"
    )
    void shouldRejectMissingOcrResultId()
            throws Exception {

        String requestBody =
                """
                {
                  "documentType": "GENERAL",
                  "forceReprocess": false
                }
                """;

        mockMvc.perform(
                        post("/api/verifications/manual")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request validation failed"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.ocrResultId"
                        ).value(
                                "OCR result ID is required"
                        )
                );

        verify(
                ocrResultService,
                never()
        ).getOcrResult(any());
    }

    @Test
    @DisplayName(
            "Should reject non-positive OCR result ID"
    )
    void shouldRejectNonPositiveOcrResultId()
            throws Exception {

        String requestBody =
                """
                {
                  "ocrResultId": 0,
                  "documentType": "GENERAL",
                  "forceReprocess": false
                }
                """;

        mockMvc.perform(
                        post("/api/verifications/manual")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.ocrResultId"
                        ).value(
                                "OCR result ID must be positive"
                        )
                );

        verify(
                ocrResultService,
                never()
        ).getOcrResult(any());
    }

    @Test
    @DisplayName(
            "Should reject invalid JSON request body"
    )
    void shouldRejectInvalidJson()
            throws Exception {

        String invalidRequestBody =
                """
                {
                  "ocrResultId": 2,
                  "documentType":
                }
                """;

        mockMvc.perform(
                        post("/api/verifications/manual")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(invalidRequestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request body is missing or contains invalid JSON"
                                )
                );

        verify(
                ocrResultService,
                never()
        ).getOcrResult(any());
    }

    @Test
    @DisplayName(
            "Should retry verification"
    )
    void shouldRetryVerification()
            throws Exception {

        VerificationResultResponse existingResult =
                createVerificationResponse();

        existingResult.setStatus(
                VerificationStatus.REVIEW_REQUIRED
        );

        VerificationResultResponse retryResponse =
                createVerificationResponse();

        retryResponse.setId(11L);
        retryResponse.setRetryCount(1);

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        when(
                verificationService.getVerificationResult(10L)
        ).thenReturn(existingResult);

        when(
                ocrResultService.getOcrResult(2L)
        ).thenReturn(ocrResult);

        when(
                verificationService.retryVerification(
                        eq(10L),
                        eq(ocrResult),
                        eq("testuser"),
                        eq("OCR result was corrected")
                )
        ).thenReturn(retryResponse);

        String requestBody =
                """
                {
                  "reason": "OCR result was corrected",
                  "refreshOcrResult": true
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/verifications/10/retry"
                        )
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification retry completed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(11)
                )
                .andExpect(
                        jsonPath("$.data.retryCount")
                                .value(1)
                );

        verify(
                verificationService
        ).getVerificationResult(10L);

        verify(
                ocrResultService
        ).getOcrResult(2L);

        verify(
                verificationService
        ).retryVerification(
                10L,
                ocrResult,
                "testuser",
                "OCR result was corrected"
        );
    }

    @Test
    @DisplayName(
            "Should reject retry without a reason"
    )
    void shouldRejectRetryWithoutReason()
            throws Exception {

        String requestBody =
                """
                {
                  "reason": "",
                  "refreshOcrResult": true
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/verifications/10/retry"
                        )
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.reason"
                        ).value(
                                "Retry reason is required"
                        )
                );

        verify(
                verificationService,
                never()
        ).getVerificationResult(any());
    }

    @Test
    @DisplayName(
            "Should get verification result by ID"
    )
    void shouldGetVerificationResult()
            throws Exception {

        when(
                verificationService.getVerificationResult(10L)
        ).thenReturn(
                createVerificationResponse()
        );

        mockMvc.perform(
                        get("/api/verifications/10")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification result retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.data.ocrResultId")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data.totalRuleCount")
                                .value(5)
                );

        verify(
                verificationService
        ).getVerificationResult(10L);
    }

    @Test
    @DisplayName(
            "Should reject non-positive verification result ID"
    )
    void shouldRejectNonPositiveVerificationResultId()
            throws Exception {

        when(
                verificationService.getVerificationResult(0L)
        ).thenThrow(
                new InvalidVerificationRequestException(
                        "Verification result ID must be a positive number"
                )
        );

        mockMvc.perform(
                        get("/api/verifications/0")
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification result ID must be a positive number"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/verifications/0"
                                )
                );

        verify(
                verificationService
        ).getVerificationResult(0L);
    }

    @Test
    @DisplayName(
            "Should return not-found error response"
    )
    void shouldReturnNotFoundResponse()
            throws Exception {

        when(
                verificationService.getVerificationResult(999L)
        ).thenThrow(
                new VerificationNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        get("/api/verifications/999")
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification result not found with ID: 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/verifications/999"
                                )
                );
    }

    @Test
    @DisplayName(
            "Should get latest document verification"
    )
    void shouldGetLatestDocumentVerification()
            throws Exception {

        when(
                verificationService.getLatestByDocumentId(1L)
        ).thenReturn(
                createVerificationResponse()
        );

        mockMvc.perform(
                        get(
                                "/api/verifications/documents/1/latest"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.documentId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("VERIFIED")
                );

        verify(
                verificationService
        ).getLatestByDocumentId(1L);
    }

    @Test
    @DisplayName(
            "Should reject non-positive document ID for latest result"
    )
    void shouldRejectNonPositiveDocumentIdForLatestResult()
            throws Exception {

        when(
                verificationService.getLatestByDocumentId(0L)
        ).thenThrow(
                new InvalidVerificationRequestException(
                        "Document ID must be a positive number"
                )
        );

        mockMvc.perform(
                        get(
                                "/api/verifications/documents/0/latest"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Document ID must be a positive number"
                                )
                );

        verify(
                verificationService
        ).getLatestByDocumentId(0L);
    }

    @Test
    @DisplayName(
            "Should get document verification history"
    )
    void shouldGetDocumentVerificationHistory()
            throws Exception {

        VerificationResultResponse firstResult =
                createVerificationResponse();

        VerificationResultResponse secondResult =
                createVerificationResponse();

        secondResult.setId(11L);
        secondResult.setStatus(
                VerificationStatus.REVIEW_REQUIRED
        );

        when(
                verificationService.getDocumentHistory(1L)
        ).thenReturn(
                List.of(
                        firstResult,
                        secondResult
                )
        );

        mockMvc.perform(
                        get(
                                "/api/verifications/documents/1/history"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.data[1].id")
                                .value(11)
                );

        verify(
                verificationService
        ).getDocumentHistory(1L);
    }

    @Test
    @DisplayName(
            "Should get verification results for a user"
    )
    void shouldGetResultsForUser()
            throws Exception {

        when(
                verificationService.getResultsForUser(
                        "testuser"
                )
        ).thenReturn(
                List.of(
                        createVerificationResponse()
                )
        );

        mockMvc.perform(
                        get(
                                "/api/verifications/users/TestUser"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data[0].requestedBy")
                                .value("testuser")
                );

        verify(
                verificationService
        ).getResultsForUser(
                "testuser"
        );
    }

    @Test
    @DisplayName(
            "Should get current authenticated user's results"
    )
    void shouldGetCurrentUserResults()
            throws Exception {

        when(
                verificationService.getResultsForUser(
                        "testuser"
                )
        ).thenReturn(
                List.of(
                        createVerificationResponse()
                )
        );

        mockMvc.perform(
                        get("/api/verifications/me")
                                .principal(authentication)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(1)
                );

        verify(
                verificationService
        ).getResultsForUser(
                "testuser"
        );
    }

    @Test
    @DisplayName(
            "Should list all verification results"
    )
    void shouldListAllVerificationResults()
            throws Exception {

        when(
                verificationService.getAllResults()
        ).thenReturn(
                List.of(
                        createVerificationResponse()
                )
        );

        mockMvc.perform(
                        get("/api/verifications")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification results retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(1)
                );

        verify(
                verificationService
        ).getAllResults();
    }

    @Test
    @DisplayName(
            "Should filter verification results by status"
    )
    void shouldFilterResultsByStatus()
            throws Exception {

        when(
                verificationService.getResultsByStatus(
                        VerificationStatus.VERIFIED
                )
        ).thenReturn(
                List.of(
                        createVerificationResponse()
                )
        );

        mockMvc.perform(
                        get("/api/verifications")
                                .param(
                                        "status",
                                        "VERIFIED"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification results with status VERIFIED retrieved successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data[0].status")
                                .value("VERIFIED")
                );

        verify(
                verificationService
        ).getResultsByStatus(
                VerificationStatus.VERIFIED
        );
    }

    @Test
    @DisplayName(
            "Should return bad request for invalid status"
    )
    void shouldRejectInvalidStatus()
            throws Exception {

        mockMvc.perform(
                        get("/api/verifications")
                                .param(
                                        "status",
                                        "UNKNOWN_STATUS"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers
                                                .containsString(
                                                        "Allowed values"
                                                )
                                )
                );
    }

    @Test
    @DisplayName(
            "Should approve verification through review endpoint"
    )
    void shouldReviewVerification()
            throws Exception {

        VerificationResultResponse response =
                createVerificationResponse();

        response.setReviewDecision(
                ReviewDecision.APPROVED
        );

        when(
                verificationService.reviewVerification(
                        eq(10L),
                        any(ReviewDecisionRequest.class),
                        eq("testuser")
                )
        ).thenReturn(response);

        String requestBody =
                """
                {
                  "decision": "APPROVED",
                  "reason": "Manually verified"
                }
                """;

        mockMvc.perform(
                        patch(
                                "/api/verifications/10/review"
                        )
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Verification review completed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.reviewDecision")
                                .value("APPROVED")
                );

        ArgumentCaptor<ReviewDecisionRequest> captor =
                ArgumentCaptor.forClass(
                        ReviewDecisionRequest.class
                );

        verify(
                verificationService
        ).reviewVerification(
                eq(10L),
                captor.capture(),
                eq("testuser")
        );

        assertThat(
                captor.getValue().getDecision()
        ).isEqualTo(
                ReviewDecision.APPROVED
        );

        assertThat(
                captor.getValue().getReason()
        ).isEqualTo(
                "Manually verified"
        );
    }

    @Test
    @DisplayName(
            "Should reject review without decision"
    )
    void shouldRejectReviewWithoutDecision()
            throws Exception {

        String requestBody =
                """
                {
                  "reason": "Manual review"
                }
                """;

        mockMvc.perform(
                        patch(
                                "/api/verifications/10/review"
                        )
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.decision"
                        ).value(
                                "Review decision is required"
                        )
                );

        verify(
                verificationService,
                never()
        ).reviewVerification(
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName(
            "Should return verification summary"
    )
    void shouldReturnSummary()
            throws Exception {

        VerificationSummaryResponse summary =
                new VerificationSummaryResponse();

        summary.setTotal(10L);
        summary.setPending(0L);
        summary.setProcessing(0L);
        summary.setVerified(6L);
        summary.setRejected(2L);
        summary.setReviewRequired(2L);
        summary.setFailed(0L);

        when(
                verificationService.getStatusSummary()
        ).thenReturn(summary);

        mockMvc.perform(
                        get(
                                "/api/verifications/summary"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.data.total")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.data.verified")
                                .value(6)
                )
                .andExpect(
                        jsonPath("$.data.rejected")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.data.reviewRequired")
                                .value(2)
                );

        verify(
                verificationService
        ).getStatusSummary();
    }

    private OcrResultInternalResponse createOcrResult() {

        OcrResultInternalResponse response =
                new OcrResultInternalResponse();

        response.setId(2L);
        response.setDocumentId(1L);
        response.setOriginalFileName(
                "document.png"
        );
        response.setContentType(
                "image/png"
        );
        response.setLanguage(
                "eng"
        );
        response.setStatus(
                "COMPLETED"
        );
        response.setExtractedText(
                "Document Number: REF-1001 "
                        + "Name: Sample User Status: Active"
        );
        response.setAverageConfidence(
                new BigDecimal("90.00")
        );
        response.setPageCount(1);
        response.setCharacterCount(60);
        response.setWordCount(8);
        response.setProcessingDurationMs(500L);
        response.setRequestedBy(
                "testuser"
        );
        response.setRetryCount(0);

        return response;
    }

    private VerificationResultResponse
    createVerificationResponse() {

        VerificationResultResponse response =
                new VerificationResultResponse();

        response.setId(10L);
        response.setDocumentId(1L);
        response.setOcrResultId(2L);
        response.setOriginalFileName(
                "document.png"
        );
        response.setDocumentType(
                "GENERAL"
        );
        response.setStatus(
                VerificationStatus.VERIFIED
        );
        response.setVerificationScore(
                new BigDecimal("95.00")
        );
        response.setOcrConfidence(
                new BigDecimal("90.00")
        );
        response.setRequestedBy(
                "testuser"
        );
        response.setTotalRuleCount(5);
        response.setPassedRuleCount(5);
        response.setFailedRuleCount(0);
        response.setWarningRuleCount(0);
        response.setSkippedRuleCount(0);
        response.setMandatoryFailureCount(0);
        response.setRetryCount(0);
        response.setSummary(
                "Verification completed successfully"
        );

        return response;
    }
}
