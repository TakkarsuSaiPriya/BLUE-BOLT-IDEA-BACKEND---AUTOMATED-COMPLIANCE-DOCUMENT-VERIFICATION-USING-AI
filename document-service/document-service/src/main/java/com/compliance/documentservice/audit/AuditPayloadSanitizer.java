package com.compliance.documentservice.audit;

import com.compliance.documentservice.dto.request.DocumentStatusUpdateRequest;
import com.compliance.documentservice.dto.request.DocumentUpdateRequest;
import com.compliance.documentservice.dto.response.ApiResponse;
import com.compliance.documentservice.dto.response.DocumentResponse;
import com.compliance.documentservice.dto.response.DocumentUploadResponse;
import com.compliance.documentservice.storage.DownloadedDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuditPayloadSanitizer {

    private final ObjectMapper objectMapper;

    public AuditPayloadSanitizer(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public String sanitizeRequest(Object[] arguments) {

        if (arguments == null || arguments.length == 0) {
            return "{}";
        }

        List<Object> safeArguments =
                new ArrayList<>();

        for (Object argument : arguments) {
            safeArguments.add(
                    sanitizeValue(argument)
            );
        }

        return serialize(safeArguments);
    }

    public String sanitizeResponse(Object response) {

        if (response == null) {
            return "{}";
        }

        Object sanitizedResponse =
                sanitizeValue(response);

        return serialize(sanitizedResponse);
    }

    private Object sanitizeValue(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof MultipartFile multipartFile) {
            return sanitizeMultipartFile(multipartFile);
        }

        if (value instanceof DocumentStatusUpdateRequest request) {
            return sanitizeStatusUpdateRequest(request);
        }

        if (value instanceof DocumentUpdateRequest request) {
            return sanitizeDocumentUpdateRequest(request);
        }

        if (value instanceof DocumentUploadResponse response) {
            return sanitizeUploadResponse(response);
        }

        if (value instanceof DocumentResponse response) {
            return sanitizeDocumentResponse(response);
        }

        if (value instanceof DownloadedDocument downloadedDocument) {
            return sanitizeDownloadedDocument(downloadedDocument);
        }

        if (value instanceof ApiResponse<?> apiResponse) {
            return sanitizeApiResponse(apiResponse);
        }

        if (value instanceof Resource resource) {

            Map<String, Object> safeValue =
                    new LinkedHashMap<>();

            safeValue.put(
                    "resource",
                    resource.getFilename()
            );

            safeValue.put(
                    "content",
                    "[BINARY_CONTENT_REDACTED]"
            );

            return safeValue;
        }

        if (value instanceof byte[]) {
            return "[BINARY_CONTENT_REDACTED]";
        }

        return value;
    }

    private Map<String, Object> sanitizeMultipartFile(
            MultipartFile file) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "originalFileName",
                file.getOriginalFilename()
        );

        payload.put(
                "contentType",
                file.getContentType()
        );

        payload.put(
                "fileSize",
                file.getSize()
        );

        payload.put(
                "empty",
                file.isEmpty()
        );

        payload.put(
                "content",
                "[BINARY_CONTENT_REDACTED]"
        );

        return payload;
    }

    private Map<String, Object> sanitizeStatusUpdateRequest(
            DocumentStatusUpdateRequest request) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "status",
                request.getStatus()
        );

        payload.put(
                "reason",
                request.getReason()
        );

        return payload;
    }

    private Map<String, Object> sanitizeDocumentUpdateRequest(
            DocumentUpdateRequest request) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "documentType",
                request.getDocumentType()
        );

        payload.put(
                "description",
                request.getDescription()
        );

        return payload;
    }

    private Map<String, Object> sanitizeUploadResponse(
            DocumentUploadResponse response) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "documentId",
                response.getDocumentId()
        );

        payload.put(
                "originalFileName",
                response.getOriginalFileName()
        );

        payload.put(
                "documentType",
                response.getDocumentType()
        );

        payload.put(
                "status",
                response.getStatus()
        );

        payload.put(
                "uploadedBy",
                response.getUploadedBy()
        );

        payload.put(
                "message",
                response.getMessage()
        );

        payload.put(
                "uploadedAt",
                response.getUploadedAt()
        );

        return payload;
    }

    private Map<String, Object> sanitizeDocumentResponse(
            DocumentResponse response) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "id",
                response.getId()
        );

        payload.put(
                "originalFileName",
                response.getOriginalFileName()
        );

        payload.put(
                "contentType",
                response.getContentType()
        );

        payload.put(
                "fileSize",
                response.getFileSize()
        );

        payload.put(
                "documentType",
                response.getDocumentType()
        );

        payload.put(
                "status",
                response.getStatus()
        );

        payload.put(
                "uploadedBy",
                response.getUploadedBy()
        );

        payload.put(
                "active",
                response.isActive()
        );

        payload.put(
                "createdAt",
                response.getCreatedAt()
        );

        payload.put(
                "updatedAt",
                response.getUpdatedAt()
        );

        return payload;
    }

    private Map<String, Object> sanitizeDownloadedDocument(
            DownloadedDocument document) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "originalFileName",
                document.getOriginalFileName()
        );

        payload.put(
                "contentType",
                document.getContentType()
        );

        payload.put(
                "fileSize",
                document.getFileSize()
        );

        payload.put(
                "content",
                "[BINARY_CONTENT_REDACTED]"
        );

        return payload;
    }

    private Map<String, Object> sanitizeApiResponse(
            ApiResponse<?> response) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "success",
                response.isSuccess()
        );

        payload.put(
                "message",
                response.getMessage()
        );

        payload.put(
                "data",
                sanitizeValue(response.getData())
        );

        payload.put(
                "timestamp",
                response.getTimestamp()
        );

        return payload;
    }

    private String serialize(Object value) {

        try {
            return objectMapper.writeValueAsString(value);

        } catch (JsonProcessingException exception) {
            return "{\"message\":\"Unable to serialize audit payload\"}";
        }
    }
}