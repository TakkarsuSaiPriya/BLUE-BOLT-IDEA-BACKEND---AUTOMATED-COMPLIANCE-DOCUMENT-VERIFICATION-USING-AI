package com.compliance.ocrservice.ocr;

import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.util.DocumentContentUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OcrProcessorResolver {

    private final List<OcrProcessor> processors;
    private final DocumentContentUtil documentContentUtil;

    public OcrProcessorResolver(
            List<OcrProcessor> processors,
            DocumentContentUtil documentContentUtil) {

        this.processors = processors;
        this.documentContentUtil =
                documentContentUtil;
    }

    public OcrProcessor resolve(
            String contentType) {

        String normalizedContentType =
                documentContentUtil
                        .normalizeContentType(
                                contentType
                        );

        return processors.stream()
                .filter(processor ->
                        processor.supports(
                                normalizedContentType
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidOcrRequestException(
                                "No OCR processor is available "
                                        + "for content type: "
                                        + normalizedContentType
                        )
                );
    }
}