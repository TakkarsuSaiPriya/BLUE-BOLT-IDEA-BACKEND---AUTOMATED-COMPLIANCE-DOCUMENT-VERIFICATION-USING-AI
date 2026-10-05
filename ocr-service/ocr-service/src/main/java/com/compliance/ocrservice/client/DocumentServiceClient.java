package com.compliance.ocrservice.client;

import com.compliance.ocrservice.config.InternalServiceFeignConfig;
import com.compliance.ocrservice.dto.request.DocumentStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "${document-service.name:document-service}",
        configuration = InternalServiceFeignConfig.class
)
public interface DocumentServiceClient {

    @GetMapping(
            value = "/api/internal/documents/{documentId}/content"
    )
    ResponseEntity<byte[]> downloadDocumentContent(
            @PathVariable("documentId")
            Long documentId
    );

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