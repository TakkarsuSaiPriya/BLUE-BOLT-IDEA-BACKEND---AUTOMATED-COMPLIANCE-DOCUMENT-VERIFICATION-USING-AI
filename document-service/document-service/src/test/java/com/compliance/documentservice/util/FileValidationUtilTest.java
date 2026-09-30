package com.compliance.documentservice.util;

import com.compliance.documentservice.config.StorageProperties;
import com.compliance.documentservice.exception.InvalidDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileValidationUtilTest {

    private FileValidationUtil fileValidationUtil;

    @BeforeEach
    void setUp() {

        StorageProperties storageProperties =
                new StorageProperties();

        storageProperties.setMaximumFileSize(
                10L * 1024L * 1024L
        );

        storageProperties.setAllowedContentTypes(
                List.of(
                        "application/pdf",
                        "image/jpeg",
                        "image/png"
                )
        );

        storageProperties.setAllowedExtensions(
                List.of(
                        "pdf",
                        "jpg",
                        "jpeg",
                        "png"
                )
        );

        fileValidationUtil =
                new FileValidationUtil(
                        storageProperties
                );
    }

    @Test
    void validateShouldAcceptValidPdf() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "application/pdf",
                        validPdfContent()
                );

        assertDoesNotThrow(
                () -> fileValidationUtil.validate(file)
        );
    }

    @Test
    void validateShouldAcceptValidPng() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.png",
                        "image/png",
                        validPngContent()
                );

        assertDoesNotThrow(
                () -> fileValidationUtil.validate(file)
        );
    }

    @Test
    void validateShouldAcceptValidJpeg() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.jpg",
                        "image/jpeg",
                        validJpegContent()
                );

        assertDoesNotThrow(
                () -> fileValidationUtil.validate(file)
        );
    }

    @Test
    void validateShouldRejectNullFile() {

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(null)
                );

        assertEquals(
                "Document file is required",
                exception.getMessage()
        );
    }

    @Test
    void validateShouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.pdf",
                        "application/pdf",
                        new byte[0]
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(file)
                );

        assertEquals(
                "Uploaded document cannot be empty",
                exception.getMessage()
        );
    }

    @Test
    void validateShouldRejectUnsupportedExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "malware.exe",
                        "application/pdf",
                        validPdfContent()
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(file)
                );

        assertEquals(
                true,
                exception.getMessage()
                        .contains(
                                "Unsupported file extension"
                        )
        );
    }

    @Test
    void validateShouldRejectUnsupportedContentType() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "application/zip",
                        validPdfContent()
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(file)
                );

        assertEquals(
                true,
                exception.getMessage()
                        .contains(
                                "Unsupported file content type"
                        )
        );
    }

    @Test
    void validateShouldRejectIncorrectFileSignature() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "fake.pdf",
                        "application/pdf",
                        "This is not a real PDF".getBytes()
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(file)
                );

        assertEquals(
                "Uploaded file content does not match "
                        + "the declared file extension",
                exception.getMessage()
        );
    }

    @Test
    void validateShouldRejectOversizedFile() {

        byte[] largeFile =
                new byte[(10 * 1024 * 1024) + 1];

        largeFile[0] = 0x25;
        largeFile[1] = 0x50;
        largeFile[2] = 0x44;
        largeFile[3] = 0x46;
        largeFile[4] = 0x2D;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "large.pdf",
                        "application/pdf",
                        largeFile
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileValidationUtil.validate(file)
                );

        assertEquals(
                true,
                exception.getMessage()
                        .contains(
                                "maximum allowed size"
                        )
        );
    }

    private byte[] validPdfContent() {

        return new byte[]{
                0x25,
                0x50,
                0x44,
                0x46,
                0x2D,
                0x31,
                0x2E,
                0x37,
                0x0A
        };
    }

    private byte[] validPngContent() {

        return new byte[]{
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
                0x00
        };
    }

    private byte[] validJpegContent() {

        return new byte[]{
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                (byte) 0xE0,
                0x00
        };
    }
}