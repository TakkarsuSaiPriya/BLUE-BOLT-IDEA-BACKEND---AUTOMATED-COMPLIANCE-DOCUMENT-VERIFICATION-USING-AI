package com.compliance.documentservice.util;

import com.compliance.documentservice.exception.InvalidDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecksumUtilTest {

    private ChecksumUtil checksumUtil;

    @BeforeEach
    void setUp() {
        checksumUtil = new ChecksumUtil();
    }

    @Test
    void calculateSha256ShouldReturnExpectedChecksum() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.txt",
                        "text/plain",
                        "hello".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        String checksum =
                checksumUtil.calculateSha256(file);

        assertEquals(
                "2cf24dba5fb0a30e26e83b2ac5b9e29e"
                        + "1b161e5c1fa7425e73043362938b9824",
                checksum
        );
    }

    @Test
    void sameContentShouldProduceSameChecksum() {

        byte[] content =
                "same-document-content".getBytes(
                        StandardCharsets.UTF_8
                );

        MockMultipartFile firstFile =
                new MockMultipartFile(
                        "file",
                        "first.pdf",
                        "application/pdf",
                        content
                );

        MockMultipartFile secondFile =
                new MockMultipartFile(
                        "file",
                        "second.pdf",
                        "application/pdf",
                        content
                );

        assertEquals(
                checksumUtil.calculateSha256(firstFile),
                checksumUtil.calculateSha256(secondFile)
        );
    }

    @Test
    void differentContentShouldProduceDifferentChecksum() {

        MockMultipartFile firstFile =
                new MockMultipartFile(
                        "file",
                        "first.pdf",
                        "application/pdf",
                        "first".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        MockMultipartFile secondFile =
                new MockMultipartFile(
                        "file",
                        "second.pdf",
                        "application/pdf",
                        "second".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertNotEquals(
                checksumUtil.calculateSha256(firstFile),
                checksumUtil.calculateSha256(secondFile)
        );
    }

    @Test
    void calculateSha256ShouldRejectEmptyFile() {

        MockMultipartFile emptyFile =
                new MockMultipartFile(
                        "file",
                        "empty.pdf",
                        "application/pdf",
                        new byte[0]
                );

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> checksumUtil.calculateSha256(
                                emptyFile
                        )
                );

        assertEquals(
                "Cannot calculate checksum for an empty file",
                exception.getMessage()
        );
    }

    @Test
    void calculateSha256ShouldRejectNullFile() {

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> checksumUtil.calculateSha256(null)
                );

        assertEquals(
                "Cannot calculate checksum for an empty file",
                exception.getMessage()
        );
    }
}