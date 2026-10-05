package com.compliance.ocrservice.ocr;

import com.compliance.ocrservice.config.OcrProperties;
import com.compliance.ocrservice.exception.InvalidOcrRequestException;
import com.compliance.ocrservice.exception.OcrProcessingException;
import com.compliance.ocrservice.util.DocumentContentUtil;
import com.compliance.ocrservice.util.TextNormalizationUtil;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class PdfOcrProcessor
        implements OcrProcessor {

    private final TesseractOcrEngine
            tesseractOcrEngine;

    private final OcrProperties
            ocrProperties;

    private final DocumentContentUtil
            documentContentUtil;

    private final TextNormalizationUtil
            textNormalizationUtil;

    public PdfOcrProcessor(
            TesseractOcrEngine tesseractOcrEngine,
            OcrProperties ocrProperties,
            DocumentContentUtil documentContentUtil,
            TextNormalizationUtil textNormalizationUtil) {

        this.tesseractOcrEngine =
                tesseractOcrEngine;

        this.ocrProperties =
                ocrProperties;

        this.documentContentUtil =
                documentContentUtil;

        this.textNormalizationUtil =
                textNormalizationUtil;
    }

    @Override
    public boolean supports(
            String contentType) {

        return documentContentUtil.isPdf(
                contentType
        );
    }

    @Override
    public OcrProcessingResult process(
            byte[] documentContent,
            String originalFileName,
            String language) {

        documentContentUtil.validateContent(
                documentContent,
                "application/pdf",
                originalFileName
        );

        long overallStartTime =
                System.currentTimeMillis();

        try (PDDocument pdfDocument =
                     Loader.loadPDF(
                             documentContent
                     )) {

            int pageCount =
                    pdfDocument.getNumberOfPages();

            validatePageCount(pageCount);

            PDFRenderer renderer =
                    new PDFRenderer(pdfDocument);

            StringBuilder extractedText =
                    new StringBuilder();

            double weightedConfidenceTotal =
                    0.0;

            int confidenceWeightTotal = 0;
            int totalCharacterCount = 0;
            int totalWordCount = 0;

            for (int pageIndex = 0;
                 pageIndex < pageCount;
                 pageIndex++) {

                BufferedImage pageImage =
                        renderer.renderImageWithDPI(
                                pageIndex,
                                ocrProperties
                                        .getRenderDpi(),
                                ImageType.RGB
                        );

                OcrProcessingResult pageResult =
                        tesseractOcrEngine.recognize(
                                pageImage,
                                language
                        );

                appendPageText(
                        extractedText,
                        pageResult.getExtractedText(),
                        pageIndex
                );

                int confidenceWeight =
                        Math.max(
                                pageResult.getWordCount(),
                                1
                        );

                weightedConfidenceTotal +=
                        pageResult
                                .getAverageConfidence()
                                * confidenceWeight;

                confidenceWeightTotal +=
                        confidenceWeight;

                totalCharacterCount +=
                        pageResult.getCharacterCount();

                totalWordCount +=
                        pageResult.getWordCount();
            }

            String normalizedText =
                    textNormalizationUtil.normalize(
                            extractedText.toString()
                    );

            double averageConfidence =
                    confidenceWeightTotal == 0
                            ? 0.0
                            : weightedConfidenceTotal
                            / confidenceWeightTotal;

            averageConfidence =
                    Math.round(
                            averageConfidence * 100.0
                    ) / 100.0;

            long duration =
                    System.currentTimeMillis()
                            - overallStartTime;

            return new OcrProcessingResult(
                    normalizedText,
                    averageConfidence,
                    pageCount,
                    totalCharacterCount,
                    totalWordCount,
                    duration
            );

        } catch (IOException exception) {

            throw new OcrProcessingException(
                    "Unable to load or render "
                            + "the PDF document",
                    exception
            );

        } catch (SecurityException exception) {

            throw new OcrProcessingException(
                    "The PDF document cannot be "
                            + "processed due to security restrictions",
                    exception
            );
        }
    }

    private void validatePageCount(
            int pageCount) {

        if (pageCount <= 0) {
            throw new InvalidOcrRequestException(
                    "PDF document does not contain any pages"
            );
        }

        if (pageCount
                > ocrProperties.getMaximumPages()) {

            throw new InvalidOcrRequestException(
                    "PDF document exceeds the maximum "
                            + "OCR page limit of "
                            + ocrProperties
                            .getMaximumPages()
            );
        }
    }

    private void appendPageText(
            StringBuilder extractedText,
            String pageText,
            int pageIndex) {

        if (extractedText.length() > 0) {
            extractedText.append(
                    System.lineSeparator()
            );

            extractedText.append(
                    System.lineSeparator()
            );
        }

        extractedText.append(
                "[Page "
                        + (pageIndex + 1)
                        + "]"
        );

        extractedText.append(
                System.lineSeparator()
        );

        if (pageText != null
                && !pageText.isBlank()) {

            extractedText.append(
                    pageText.trim()
            );
        }
    }
}