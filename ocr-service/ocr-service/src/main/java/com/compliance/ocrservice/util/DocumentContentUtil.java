package com.compliance.ocrservice.util;

import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class DocumentContentUtil {

    private static final Set<String>
            SUPPORTED_IMAGE_CONTENT_TYPES =
            Set.of(
                    "image/png",
                    "image/jpeg",
                    "image/jpg"
            );

    private static final Set<String>
            SUPPORTED_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "image/png",
                    "image/jpeg",
                    "image/jpg"
            );

    public void validateContent(
            byte[] content,
            String contentType,
            String originalFileName) {

        if (content == null
                || content.length == 0) {

            throw new InvalidOcrRequestException(
                    "Downloaded document content is empty"
            );
        }

        String normalizedContentType =
                normalizeContentType(contentType);

        if (!SUPPORTED_CONTENT_TYPES.contains(
                normalizedContentType)) {

            throw new InvalidOcrRequestException(
                    "Unsupported OCR content type: "
                            + normalizedContentType
            );
        }

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new InvalidOcrRequestException(
                    "Document filename is required "
                            + "for OCR processing"
            );
        }

        validateSignature(
                content,
                normalizedContentType
        );
    }

    public boolean isPdf(
            String contentType) {

        return "application/pdf".equals(
                normalizeContentType(contentType)
        );
    }

    public boolean isImage(
            String contentType) {

        return SUPPORTED_IMAGE_CONTENT_TYPES
                .contains(
                        normalizeContentType(
                                contentType
                        )
                );
    }

    public String normalizeContentType(
            String contentType) {

        if (contentType == null
                || contentType.isBlank()) {

            return "application/octet-stream";
        }

        int semicolonIndex =
                contentType.indexOf(';');

        String normalizedType =
                semicolonIndex >= 0
                        ? contentType.substring(
                        0,
                        semicolonIndex
                )
                        : contentType;

        return normalizedType
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateSignature(
            byte[] content,
            String contentType) {

        boolean valid =
                switch (contentType) {
                    case "application/pdf" ->
                            hasPdfSignature(content);

                    case "image/png" ->
                            hasPngSignature(content);

                    case "image/jpeg", "image/jpg" ->
                            hasJpegSignature(content);

                    default -> false;
                };

        if (!valid) {
            throw new InvalidOcrRequestException(
                    "Document content does not match "
                            + "the declared content type"
            );
        }
    }

    private boolean hasPdfSignature(
            byte[] content) {

        return content.length >= 5
                && content[0] == 0x25
                && content[1] == 0x50
                && content[2] == 0x44
                && content[3] == 0x46
                && content[4] == 0x2D;
    }

    private boolean hasPngSignature(
            byte[] content) {

        return content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A;
    }

    private boolean hasJpegSignature(
            byte[] content) {

        return content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF;
    }
}