package com.compliance.verificationservice.service.impl;

import com.compliance.verificationservice.client.OcrServiceClient;
import com.compliance.verificationservice.dto.internal.InternalApiResponse;
import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import com.compliance.verificationservice.exception.ExternalServiceException;
import com.compliance.verificationservice.exception.InvalidVerificationRequestException;
import com.compliance.verificationservice.service.interfaces.OcrResultService;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OcrResultServiceImpl
        implements OcrResultService {

    private static final String SERVICE_NAME =
            "ocr-service";

    private final OcrServiceClient ocrServiceClient;

    public OcrResultServiceImpl(
            OcrServiceClient ocrServiceClient) {

        this.ocrServiceClient =
                ocrServiceClient;
    }

    @Override
    public OcrResultInternalResponse getOcrResult(
            Long ocrResultId) {

        validateOcrResultId(
                ocrResultId
        );

        try {

            ResponseEntity<
                    InternalApiResponse<OcrResultInternalResponse>>
                    response =
                    ocrServiceClient.getOcrResult(
                            ocrResultId
                    );

            validateHttpResponse(
                    response,
                    ocrResultId
            );

            InternalApiResponse<OcrResultInternalResponse>
                    responseBody =
                    response.getBody();

            validateResponseBody(
                    responseBody,
                    ocrResultId
            );

            OcrResultInternalResponse ocrResult =
                    responseBody.getData();

            validateOcrResult(
                    ocrResult,
                    ocrResultId
            );

            return ocrResult;

        } catch (ExternalServiceException exception) {

            throw exception;

        } catch (FeignException exception) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    exception.status(),
                    "Unable to retrieve OCR result with ID: "
                            + ocrResultId,
                    exception
            );

        } catch (Exception exception) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "Unexpected error while retrieving OCR result "
                            + "with ID: "
                            + ocrResultId,
                    exception
            );
        }
    }

    private void validateOcrResultId(
            Long ocrResultId) {

        if (ocrResultId == null
                || ocrResultId <= 0) {

            throw new InvalidVerificationRequestException(
                    "OCR result ID must be a positive number"
            );
        }
    }

    private void validateHttpResponse(
            ResponseEntity<
                    InternalApiResponse<OcrResultInternalResponse>>
                    response,
            Long ocrResultId) {

        if (response == null) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned no response for OCR result ID: "
                            + ocrResultId
            );
        }

        if (!response.getStatusCode().is2xxSuccessful()) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    response.getStatusCode().value(),
                    "OCR Service returned an unsuccessful response "
                            + "for OCR result ID: "
                            + ocrResultId
            );
        }
    }

    private void validateResponseBody(
            InternalApiResponse<OcrResultInternalResponse>
                    responseBody,
            Long ocrResultId) {

        if (responseBody == null) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned an empty response body "
                            + "for OCR result ID: "
                            + ocrResultId
            );
        }

        if (!responseBody.isSuccess()) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service reported failure for OCR result ID "
                            + ocrResultId
                            + ": "
                            + safeMessage(
                            responseBody.getMessage()
                    )
            );
        }

        if (responseBody.getData() == null) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned no OCR data for result ID: "
                            + ocrResultId
            );
        }
    }

    private void validateOcrResult(
            OcrResultInternalResponse ocrResult,
            Long requestedOcrResultId) {

        if (ocrResult.getId() == null
                || ocrResult.getId() <= 0) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned an invalid OCR result ID"
            );
        }

        if (!requestedOcrResultId.equals(
                ocrResult.getId())) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned a different OCR result ID"
            );
        }

        if (ocrResult.getDocumentId() == null
                || ocrResult.getDocumentId() <= 0) {

            throw new ExternalServiceException(
                    SERVICE_NAME,
                    "OCR Service returned an invalid document ID"
            );
        }
    }

    private String safeMessage(
            String message) {

        if (message == null
                || message.isBlank()) {

            return "No failure message was provided";
        }

        return message.trim();
    }
}