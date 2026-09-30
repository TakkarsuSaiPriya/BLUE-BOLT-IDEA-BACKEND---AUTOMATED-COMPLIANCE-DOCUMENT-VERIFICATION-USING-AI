package com.compliance.documentservice.util;

import com.compliance.documentservice.exception.InvalidDocumentException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

public final class FileNameUtil {

    private FileNameUtil() {
    }

    public static String sanitizeOriginalFileName(
            String originalFileName) {

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new InvalidDocumentException(
                    "Uploaded file must have a valid filename"
            );
        }

        String normalizedName =
                originalFileName.replace("\\", "/");

        String fileName =
                Paths.get(normalizedName)
                        .getFileName()
                        .toString()
                        .trim();

        if (fileName.isBlank()) {
            throw new InvalidDocumentException(
                    "Uploaded file must have a valid filename"
            );
        }

        String sanitizedName =
                fileName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        sanitizedName =
                sanitizedName.replaceAll(
                        "_+",
                        "_"
                );

        if (sanitizedName.equals(".")
                || sanitizedName.equals("..")
                || sanitizedName.isBlank()) {

            throw new InvalidDocumentException(
                    "Uploaded filename is invalid"
            );
        }

        if (sanitizedName.length() > 255) {
            throw new InvalidDocumentException(
                    "Uploaded filename cannot exceed 255 characters"
            );
        }

        return sanitizedName;
    }

    public static String extractExtension(
            String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int lastDotIndex =
                fileName.lastIndexOf('.');

        if (lastDotIndex < 0
                || lastDotIndex == fileName.length() - 1) {

            return "";
        }

        return fileName
                .substring(lastDotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    public static String createStoredFileName(
            String originalFileName) {

        String sanitizedFileName =
                sanitizeOriginalFileName(
                        originalFileName
                );

        String extension =
                extractExtension(sanitizedFileName);

        String uniqueName =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        if (extension.isBlank()) {
            return uniqueName;
        }

        return uniqueName + "." + extension;
    }

    public static void validateResolvedPath(
            Path uploadDirectory,
            Path resolvedPath) {

        Path normalizedUploadDirectory =
                uploadDirectory
                        .toAbsolutePath()
                        .normalize();

        Path normalizedResolvedPath =
                resolvedPath
                        .toAbsolutePath()
                        .normalize();

        if (!normalizedResolvedPath.startsWith(
                normalizedUploadDirectory)) {

            throw new InvalidDocumentException(
                    "Invalid file storage path"
            );
        }
    }
}