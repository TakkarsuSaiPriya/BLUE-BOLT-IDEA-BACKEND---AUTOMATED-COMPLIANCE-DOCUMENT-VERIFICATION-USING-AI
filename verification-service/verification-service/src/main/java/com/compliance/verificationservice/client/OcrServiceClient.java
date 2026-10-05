package com.compliance.verificationservice.client;

import com.compliance.verificationservice.config.InternalServiceFeignConfig;
import com.compliance.verificationservice.dto.internal.InternalApiResponse;
import com.compliance.verificationservice.dto.internal.OcrResultInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "${ocr-service.name:ocr-service}",
        configuration = InternalServiceFeignConfig.class
)
public interface OcrServiceClient {

    @GetMapping(
            value = "/api/internal/ocr/results/{ocrResultId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<
            InternalApiResponse<OcrResultInternalResponse>>
    getOcrResult(
            @PathVariable("ocrResultId")
            Long ocrResultId
    );
}