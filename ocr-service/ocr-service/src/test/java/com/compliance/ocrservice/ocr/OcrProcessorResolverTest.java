package com.compliance.ocrservice.ocr;

import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.util.DocumentContentUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrProcessorResolverTest {

    @Mock
    private OcrProcessor pdfProcessor;

    @Mock
    private OcrProcessor imageProcessor;

    @Mock
    private DocumentContentUtil documentContentUtil;

    private OcrProcessorResolver
            ocrProcessorResolver;

    @BeforeEach
    void setUp() {

        ocrProcessorResolver =
                new OcrProcessorResolver(
                        List.of(
                                pdfProcessor,
                                imageProcessor
                        ),
                        documentContentUtil
                );
    }

    @Test
    void resolveShouldReturnPdfProcessor() {

        when(
                documentContentUtil
                        .normalizeContentType(
                                "application/pdf"
                        )
        ).thenReturn(
                "application/pdf"
        );

        when(
                pdfProcessor.supports(
                        "application/pdf"
                )
        ).thenReturn(true);

        OcrProcessor result =
                ocrProcessorResolver.resolve(
                        "application/pdf"
                );

        assertSame(
                pdfProcessor,
                result
        );

        verify(
                documentContentUtil
        ).normalizeContentType(
                "application/pdf"
        );

        verify(
                pdfProcessor
        ).supports(
                "application/pdf"
        );
    }

    @Test
    void resolveShouldReturnImageProcessor() {

        when(
                documentContentUtil
                        .normalizeContentType(
                                "IMAGE/PNG"
                        )
        ).thenReturn(
                "image/png"
        );

        when(
                pdfProcessor.supports(
                        "image/png"
                )
        ).thenReturn(false);

        when(
                imageProcessor.supports(
                        "image/png"
                )
        ).thenReturn(true);

        OcrProcessor result =
                ocrProcessorResolver.resolve(
                        "IMAGE/PNG"
                );

        assertSame(
                imageProcessor,
                result
        );
    }

    @Test
    void resolveShouldThrowWhenNoProcessorSupportsContentType() {

        when(
                documentContentUtil
                        .normalizeContentType(
                                "application/zip"
                        )
        ).thenReturn(
                "application/zip"
        );

        when(
                pdfProcessor.supports(
                        "application/zip"
                )
        ).thenReturn(false);

        when(
                imageProcessor.supports(
                        "application/zip"
                )
        ).thenReturn(false);

        assertThrows(
                InvalidOcrRequestException.class,
                () -> ocrProcessorResolver.resolve(
                        "application/zip"
                )
        );
    }
}