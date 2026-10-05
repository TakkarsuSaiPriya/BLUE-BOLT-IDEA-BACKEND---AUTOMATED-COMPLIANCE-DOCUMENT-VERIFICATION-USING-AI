package com.compliance.verificationservice.mapper;

import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.dto.response.RuleResultResponse;
import com.compliance.verificationservice.dto.response.VerificationResultResponse;
import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleCategory;
import com.compliance.verificationservice.enums.RuleOutcome;
import com.compliance.verificationservice.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationResultMapperTest {

    private VerificationResultMapper mapper;

    @BeforeEach
    void setUp() {

        mapper =
                new VerificationResultMapper();
    }

    @Test
    @DisplayName(
            "Should create pending verification result from OCR response"
    )
    void shouldCreateVerificationResult() {

        OcrResultInternalResponse ocrResult =
                createOcrResult();

        VerificationResult result =
                mapper.createVerificationResult(
                        ocrResult,
                        "GENERAL",
                        "TestUser"
                );

        assertThat(result.getDocumentId())
                .isEqualTo(1L);

        assertThat(result.getOcrResultId())
                .isEqualTo(2L);

        assertThat(result.getStatus())
                .isEqualTo(
                        VerificationStatus.PENDING
                );

        assertThat(result.getOcrConfidence())
                .isEqualByComparingTo(
                        "91.50"
                );

        assertThat(result.getRequestedBy())
                .isEqualTo(
                        "testuser"
                );

        assertThat(result.getTotalRuleCount())
                .isZero();
    }

    @Test
    @DisplayName(
            "Should map entity and rule results to response"
    )
    void shouldMapEntityToResponse() {

        VerificationResult entity =
                new VerificationResult();

        entity.setId(
                10L
        );

        entity.setDocumentId(
                1L
        );

        entity.setOcrResultId(
                2L
        );

        entity.setDocumentType(
                "GENERAL"
        );

        entity.setStatus(
                VerificationStatus.VERIFIED
        );

        entity.setVerificationScore(
                new BigDecimal("95.00")
        );

        entity.setOcrConfidence(
                new BigDecimal("91.50")
        );

        entity.setTotalRuleCount(
                1
        );

        entity.setPassedRuleCount(
                1
        );

        entity.setRequestedBy(
                "testuser"
        );

        VerificationRuleResult ruleResult =
                new VerificationRuleResult();

        ruleResult.setId(
                20L
        );

        ruleResult.setRuleCode(
                "EMPTY_TEXT"
        );

        ruleResult.setRuleName(
                "OCR text availability"
        );

        ruleResult.setCategory(
                RuleCategory.CONTENT
        );

        ruleResult.setOutcome(
                RuleOutcome.PASSED
        );

        ruleResult.setMandatory(
                true
        );

        ruleResult.setWeight(
                new BigDecimal("30.00")
        );

        ruleResult.setScoreAwarded(
                new BigDecimal("30.00")
        );

        ruleResult.setExecutionOrder(
                10
        );

        entity.addRuleResult(
                ruleResult
        );

        VerificationResultResponse response =
                mapper.toResponse(
                        entity
                );

        assertThat(response.getId())
                .isEqualTo(10L);

        assertThat(response.getStatus())
                .isEqualTo(
                        VerificationStatus.VERIFIED
                );

        assertThat(response.getVerificationScore())
                .isEqualByComparingTo(
                        "95.00"
                );

        assertThat(response.getRuleResults())
                .hasSize(1);

        RuleResultResponse mappedRule =
                response.getRuleResults().get(0);

        assertThat(mappedRule.getRuleCode())
                .isEqualTo(
                        "EMPTY_TEXT"
                );

        assertThat(mappedRule.getOutcome())
                .isEqualTo(
                        RuleOutcome.PASSED
                );

        assertThat(mappedRule.getScoreAwarded())
                .isEqualByComparingTo(
                        "30.00"
                );
    }

    @Test
    @DisplayName(
            "Should return null when mapping null entity"
    )
    void shouldReturnNullForNullEntity() {

        assertThat(
                mapper.toResponse(
                        null
                )
        ).isNull();
    }

    @Test
    @DisplayName(
            "Should reject OCR result without valid identifiers"
    )
    void shouldRejectInvalidOcrResult() {

        OcrResultInternalResponse ocrResult =
                new OcrResultInternalResponse();

        assertThatThrownBy(() ->
                mapper.createVerificationResult(
                        ocrResult,
                        "GENERAL",
                        "testuser"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "OCR result ID"
                );
    }

    private OcrResultInternalResponse createOcrResult() {

        OcrResultInternalResponse result =
                new OcrResultInternalResponse();

        result.setId(
                2L
        );

        result.setDocumentId(
                1L
        );

        result.setOriginalFileName(
                "sample.png"
        );

        result.setStatus(
                "COMPLETED"
        );

        result.setExtractedText(
                "Document Number: REF-1001"
        );

        result.setAverageConfidence(
                new BigDecimal("91.50")
        );

        result.setPageCount(
                1
        );

        result.setCharacterCount(
                30
        );

        result.setWordCount(
                4
        );

        return result;
    }
}