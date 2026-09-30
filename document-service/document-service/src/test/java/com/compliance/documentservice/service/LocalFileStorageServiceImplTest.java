package com.compliance.documentservice.service;

import com.compliance.documentservice.exception.InvalidDocumentException;
import com.compliance.documentservice.service.impl.LocalFileStorageServiceImpl;
import com.compliance.documentservice.storage.StoredFile;
import com.compliance.documentservice.util.FileValidationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceImplTest {

    @TempDir
    Path temporaryDirectory;

    @Mock
    private FileValidationUtil fileValidationUtil;

    private LocalFileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {

        fileStorageService =
                new LocalFileStorageServiceImpl(
                        temporaryDirectory,
                        fileValidationUtil
                );
    }

    @AfterEach
    void tearDown() throws IOException {

        try (var files =
                     Files.list(temporaryDirectory)) {

            files.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        }
    }

//    @Test
//    void storeShouldSaveFile() throws IOException {
//
//        byte[] content =
//                new byte[]{
//                        0x25,
//                        0x50,
//                        0x44,
//                        0x46,
//                        0x2D
//                };
//
//        MockMultipartFile file =
//                new MockMultipartFile(
//                        "file",
//                        "pan-card.pdf",
//                        "application/pdf",
//                        content
//                );
//
//        StoredFile storedFile =
//                fileStorageService.store(file);
//
//        assertNotNull(storedFile);
//        assertEquals(
//                "pan-card.pdf",
//                storedFile.getOriginalFileName()
//        );
//        assertEquals(
//                "application/pdf",
//                storedFile.getContentType()
//        );
//        assertEquals("pdf", storedFile.getFileExtension());
//        assertEquals(content.length, storedFile.getFileSize());
//        assertTrue(
//                Files.exists(
//                        Path.of(storedFile.getStoragePath())
//                )
//        );
//
//        verify(fileValidationUtil).validate(file);
//    }

    @Test
    void loadAsResourceShouldLoadStoredFile()
            throws IOException {

        String storedFileName =
                "stored-document.pdf";

        Path storedPath =
                temporaryDirectory.resolve(
                        storedFileName
                );

        Files.write(
                storedPath,
                "test-content".getBytes()
        );

        Resource resource =
                fileStorageService.loadAsResource(
                        storedFileName
                );

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(
                storedFileName,
                resource.getFilename()
        );
    }

    @Test
    void existsShouldReturnTrueForExistingFile()
            throws IOException {

        String storedFileName =
                "existing.pdf";

        Files.write(
                temporaryDirectory.resolve(
                        storedFileName
                ),
                "content".getBytes()
        );

        assertTrue(
                fileStorageService.exists(
                        storedFileName
                )
        );
    }

    @Test
    void existsShouldReturnFalseForMissingFile() {

        assertFalse(
                fileStorageService.exists(
                        "missing.pdf"
                )
        );
    }

    @Test
    void deleteShouldDeleteStoredFile()
            throws IOException {

        String storedFileName =
                "delete-document.pdf";

        Path storedPath =
                temporaryDirectory.resolve(
                        storedFileName
                );

        Files.write(
                storedPath,
                "content".getBytes()
        );

        fileStorageService.delete(
                storedFileName
        );

        assertFalse(Files.exists(storedPath));
    }

    @Test
    void pathTraversalShouldBeRejected() {

        InvalidDocumentException exception =
                assertThrows(
                        InvalidDocumentException.class,
                        () -> fileStorageService
                                .loadAsResource(
                                        "../secret.txt"
                                )
                );

        assertEquals(
                "Stored filename contains an invalid path",
                exception.getMessage()
        );
    }
}