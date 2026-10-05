package com.compliance.ocrservice.config;

import com.compliance.ocrservice.exception.OcrProcessingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class OcrConfig {

    private final OcrProperties ocrProperties;

    public OcrConfig(
            OcrProperties ocrProperties) {

        this.ocrProperties = ocrProperties;
    }

    @Bean(name = "ocrTemporaryDirectory")
    public Path ocrTemporaryDirectory() {

        try {
            Path temporaryDirectory =
                    Paths.get(
                                    ocrProperties
                                            .getTemporaryDirectory()
                            )
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(
                    temporaryDirectory
            );

            if (!Files.isDirectory(
                    temporaryDirectory)) {

                throw new OcrProcessingException(
                        "Configured OCR temporary location "
                                + "is not a directory"
                );
            }

            if (!Files.isWritable(
                    temporaryDirectory)) {

                throw new OcrProcessingException(
                        "Configured OCR temporary directory "
                                + "is not writable"
                );
            }

            return temporaryDirectory;

        } catch (IOException exception) {

            throw new OcrProcessingException(
                    "Unable to initialize OCR "
                            + "temporary directory",
                    exception
            );
        }
    }

    @Bean(name = "tessdataDirectory")
    public Path tessdataDirectory() {

        return Paths.get(
                        ocrProperties.getDataPath()
                )
                .toAbsolutePath()
                .normalize();
    }
}