package com.compliance.documentservice.config;

import com.compliance.documentservice.exception.FileStorageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    private final StorageProperties storageProperties;

    public StorageConfig(
            StorageProperties storageProperties) {

        this.storageProperties = storageProperties;
    }

    @Bean(name = "uploadDirectory")
    public Path uploadDirectory() {

        try {
            Path uploadDirectory =
                    Paths.get(
                                    storageProperties
                                            .getUploadDirectory()
                            )
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadDirectory);

            if (!Files.isDirectory(uploadDirectory)) {
                throw new FileStorageException(
                        "Configured upload location is not a directory"
                );
            }

            if (!Files.isWritable(uploadDirectory)) {
                throw new FileStorageException(
                        "Configured upload directory is not writable"
                );
            }

            return uploadDirectory;

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to initialize upload directory",
                    exception
            );
        }
    }
}