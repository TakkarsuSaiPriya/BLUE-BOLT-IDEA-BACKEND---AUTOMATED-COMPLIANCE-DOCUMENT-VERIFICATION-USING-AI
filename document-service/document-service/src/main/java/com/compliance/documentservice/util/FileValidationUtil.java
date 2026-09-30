package com.compliance.documentservice.util;

import com.compliance.documentservice.config.StorageProperties;
import com.compliance.documentservice.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Component
public class FileValidationUtil {

    private final StorageProperties storageProperties;

    public FileValidationUtil(
            StorageProperties storageProperties) {

        this.storageProperties = storageProperties;
    }

    public void validate(MultipartFile file) {

        validateFilePresence(file);

        String originalFileName =
                FileNameUtil.sanitizeOriginalFileName(
                        file.getOriginalFilename()
                );

        String extension =
                FileNameUtil.extractExtension(
                        originalFileName
                );

        validateFileSize(file);
        validateExtension(extension);
        validateContentType(file.getContentType());
        validateFileSignature(file, extension);
    }

    private void validateFilePresence(
            MultipartFile file) {

        if (file == null) {
            throw new InvalidDocumentException(
                    "Document file is required"
            );
        }

        if (file.isEmpty()) {
            throw new InvalidDocumentException(
                    "Uploaded document cannot be empty"
            );
        }

        if (file.getSize() <= 0) {
            throw new InvalidDocumentException(
                    "Uploaded document must contain data"
            );
        }
    }

    private void validateFileSize(
            MultipartFile file) {

        long maximumFileSize =
                storageProperties.getMaximumFileSize();

        if (file.getSize() > maximumFileSize) {
            throw new InvalidDocumentException(
                    "Uploaded document exceeds the maximum "
                            + "allowed size of "
                            + formatBytes(maximumFileSize)
            );
        }
    }

    private void validateExtension(
            String extension) {

        if (extension == null || extension.isBlank()) {
            throw new InvalidDocumentException(
                    "Uploaded document must have a file extension"
            );
        }

        List<String> allowedExtensions =
                storageProperties.getAllowedExtensions();

        boolean allowed =
                allowedExtensions
                        .stream()
                        .map(value ->
                                value.toLowerCase(Locale.ROOT))
                        .anyMatch(extension::equals);

        if (!allowed) {
            throw new InvalidDocumentException(
                    "Unsupported file extension: "
                            + extension
                            + ". Allowed extensions are: "
                            + allowedExtensions
            );
        }
    }

    private void validateContentType(
            String contentType) {

        if (contentType == null
                || contentType.isBlank()) {

            throw new InvalidDocumentException(
                    "Uploaded document content type is missing"
            );
        }

        List<String> allowedContentTypes =
                storageProperties.getAllowedContentTypes();

        boolean allowed =
                allowedContentTypes
                        .stream()
                        .anyMatch(value ->
                                value.equalsIgnoreCase(contentType));

        if (!allowed) {
            throw new InvalidDocumentException(
                    "Unsupported file content type: "
                            + contentType
                            + ". Allowed content types are: "
                            + allowedContentTypes
            );
        }
    }

    private void validateFileSignature(
            MultipartFile file,
            String extension) {

        byte[] header = readHeader(file, 12);

        boolean validSignature =
                switch (extension) {
                    case "pdf" -> isPdf(header);
                    case "png" -> isPng(header);
                    case "jpg", "jpeg" -> isJpeg(header);
                    default -> false;
                };

        if (!validSignature) {
            throw new InvalidDocumentException(
                    "Uploaded file content does not match "
                            + "the declared file extension"
            );
        }
    }

    private byte[] readHeader(
            MultipartFile file,
            int maximumBytes) {

        try (InputStream inputStream =
                     file.getInputStream()) {

            return inputStream.readNBytes(maximumBytes);

        } catch (IOException exception) {
            throw new InvalidDocumentException(
                    "Unable to inspect uploaded file",
                    exception
            );
        }
    }

    private boolean isPdf(byte[] header) {

        return header.length >= 5
                && header[0] == 0x25
                && header[1] == 0x50
                && header[2] == 0x44
                && header[3] == 0x46
                && header[4] == 0x2D;
    }

    private boolean isPng(byte[] header) {

        return header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private boolean isJpeg(byte[] header) {

        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private String formatBytes(long bytes) {

        long megabyte =
                1024L * 1024L;

        if (bytes >= megabyte) {
            return (bytes / megabyte) + " MB";
        }

        long kilobyte = 1024L;

        if (bytes >= kilobyte) {
            return (bytes / kilobyte) + " KB";
        }

        return bytes + " bytes";
    }
}