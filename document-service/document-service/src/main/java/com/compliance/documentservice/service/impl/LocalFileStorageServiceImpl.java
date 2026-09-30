package com.compliance.documentservice.service.impl;

import com.compliance.documentservice.exception.FileStorageException;
import com.compliance.documentservice.exception.InvalidDocumentException;
import com.compliance.documentservice.service.interfaces.FileStorageService;
import com.compliance.documentservice.storage.StoredFile;
import com.compliance.documentservice.util.FileNameUtil;
import com.compliance.documentservice.util.FileValidationUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageServiceImpl
        implements FileStorageService {

    private final Path uploadDirectory;
    private final FileValidationUtil fileValidationUtil;

    public LocalFileStorageServiceImpl(
            @Qualifier("uploadDirectory")
            Path uploadDirectory,
            FileValidationUtil fileValidationUtil) {

        this.uploadDirectory =
                uploadDirectory
                        .toAbsolutePath()
                        .normalize();

        this.fileValidationUtil =
                fileValidationUtil;
    }

    @Override
    public StoredFile store(
            MultipartFile file) {

        fileValidationUtil.validate(file);

        String originalFileName =
                FileNameUtil.sanitizeOriginalFileName(
                        file.getOriginalFilename()
                );

        String storedFileName =
                FileNameUtil.createStoredFileName(
                        originalFileName
                );

        String fileExtension =
                FileNameUtil.extractExtension(
                        originalFileName
                );

        Path targetPath =
                resolveStoredFilePath(
                        storedFileName
                );

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    targetPath,
                    StandardCopyOption.COPY_ATTRIBUTES
            );

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    targetPath.toString(),
                    fileExtension,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (FileAlreadyExistsException exception) {
            throw new FileStorageException(
                    "A stored file with the generated name already exists",
                    exception
            );

        } catch (IOException exception) {

            deleteQuietly(targetPath);

            throw new FileStorageException(
                    "Unable to store uploaded document: "
                            + originalFileName,
                    exception
            );
        }
    }

    @Override
    public Resource loadAsResource(
            String storedFileName) {

        Path filePath =
                resolveStoredFilePath(
                        storedFileName
                );

        try {
            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new FileStorageException(
                        "Stored document is not available for download"
                );
            }

            return resource;

        } catch (MalformedURLException exception) {
            throw new FileStorageException(
                    "Unable to load stored document",
                    exception
            );
        }
    }

    @Override
    public boolean exists(
            String storedFileName) {

        Path filePath =
                resolveStoredFilePath(
                        storedFileName
                );

        return Files.exists(filePath)
                && Files.isRegularFile(filePath);
    }

    @Override
    public void delete(
            String storedFileName) {

        Path filePath =
                resolveStoredFilePath(
                        storedFileName
                );

        try {
            Files.deleteIfExists(filePath);

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to delete stored document",
                    exception
            );
        }
    }

    private Path resolveStoredFilePath(
            String storedFileName) {

        if (storedFileName == null
                || storedFileName.isBlank()) {

            throw new InvalidDocumentException(
                    "Stored filename is required"
            );
        }

        if (storedFileName.contains("/")
                || storedFileName.contains("\\")
                || storedFileName.contains("..")) {

            throw new InvalidDocumentException(
                    "Stored filename contains an invalid path"
            );
        }

        Path resolvedPath =
                uploadDirectory
                        .resolve(storedFileName)
                        .normalize();

        FileNameUtil.validateResolvedPath(
                uploadDirectory,
                resolvedPath
        );

        return resolvedPath;
    }

    private void deleteQuietly(
            Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}