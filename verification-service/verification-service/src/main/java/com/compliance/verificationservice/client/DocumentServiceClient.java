package com.compliance.verificationservice.client;

import com.compliance.verificationservice.config.InternalServiceFeignConfig;
import com.compliance.verificationservice.dto.internal.DocumentStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "${document-service.name:document-service}",
        configuration = InternalServiceFeignConfig.class
)
public interface DocumentServiceClient {

    @PatchMapping(
            value = "/api/internal/documents/{documentId}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> updateDocumentStatus(
            @PathVariable("documentId")
            Long documentId,
            @RequestBody
            DocumentStatusUpdateRequest request
    );
}