package com.compliance.ocrservice.ocr;

import com.compliance.ocrservice.config.OcrProperties;
import com.compliance.ocrservice.exception.OcrProcessingException;
import com.compliance.ocrservice.util.ConfidenceUtil;
import com.compliance.ocrservice.util.TextNormalizationUtil;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class TesseractOcrEngine {

    private final OcrProperties ocrProperties;
    private final Path tessdataDirectory;
    private final TextNormalizationUtil textNormalizationUtil;
    private final ConfidenceUtil confidenceUtil;

    public TesseractOcrEngine(
            OcrProperties ocrProperties,
            @Qualifier("tessdataDirectory")
            Path tessdataDirectory,
            TextNormalizationUtil textNormalizationUtil,
            ConfidenceUtil confidenceUtil) {

        this.ocrProperties = ocrProperties;
        this.tessdataDirectory =
                tessdataDirectory
                        .toAbsolutePath()
                        .normalize();
        this.textNormalizationUtil =
                textNormalizationUtil;
        this.confidenceUtil = confidenceUtil;
    }

    public OcrProcessingResult recognize(
            BufferedImage image,
            String language) {

        if (image == null) {
            throw new OcrProcessingException(
                    "OCR image cannot be null"
            );
        }

        String normalizedLanguage =
                normalizeLanguage(language);

        validateLanguageData(
                normalizedLanguage
        );

        long startTime =
                System.currentTimeMillis();

        try {
            Tesseract tesseract =
                    createTesseract(
                            normalizedLanguage
                    );

            String rawText =
                    tesseract.doOCR(image);

            String normalizedText =
                    textNormalizationUtil.normalize(
                            rawText
                    );

            List<Word> words =
                    tesseract.getWords(
                            image,
                            ITessAPI
                                    .TessPageIteratorLevel
                                    .RIL_WORD
                    );

            double averageConfidence =
                    confidenceUtil
                            .calculateAverageConfidence(
                                    words
                            );

            long duration =
                    System.currentTimeMillis()
                            - startTime;

            return new OcrProcessingResult(
                    normalizedText,
                    averageConfidence,
                    1,
                    textNormalizationUtil
                            .countCharacters(
                                    normalizedText
                            ),
                    textNormalizationUtil
                            .countWords(
                                    normalizedText
                            ),
                    duration
            );

        } catch (TesseractException exception) {

            throw new OcrProcessingException(
                    "Tesseract was unable to "
                            + "extract document text",
                    exception
            );

        } catch (RuntimeException exception) {

            if (exception
                    instanceof OcrProcessingException) {

                throw exception;
            }

            throw new OcrProcessingException(
                    "Unexpected Tesseract OCR failure",
                    exception
            );
        }
    }

    private Tesseract createTesseract(
            String language) {

        Tesseract tesseract =
                new Tesseract();

        tesseract.setDatapath(
                tessdataDirectory.toString()
        );

        tesseract.setLanguage(language);

        tesseract.setPageSegMode(
                ocrProperties
                        .getPageSegmentationMode()
        );

        tesseract.setOcrEngineMode(
                ocrProperties.getEngineMode()
        );

        tesseract.setVariable(
                "user_defined_dpi",
                String.valueOf(
                        ocrProperties.getRenderDpi()
                )
        );

        return tesseract;
    }

    private String normalizeLanguage(
            String language) {

        if (language == null
                || language.isBlank()) {

            return ocrProperties.getLanguage();
        }

        String normalizedLanguage =
                language.trim()
                        .toLowerCase();

        if (!normalizedLanguage.matches(
                "[a-z0-9_+\\-]{2,30}")) {

            throw new OcrProcessingException(
                    "Invalid OCR language code: "
                            + normalizedLanguage
            );
        }

        return normalizedLanguage;
    }

    private void validateLanguageData(
            String language) {

        if (!Files.exists(tessdataDirectory)
                || !Files.isDirectory(
                tessdataDirectory)) {

            throw new OcrProcessingException(
                    "Tesseract data directory does "
                            + "not exist: "
                            + tessdataDirectory
            );
        }

        String[] languages =
                language.split("\\+");

        for (String languageCode : languages) {

            Path languageFile =
                    tessdataDirectory.resolve(
                            languageCode
                                    + ".traineddata"
                    );

            if (!Files.exists(languageFile)
                    || !Files.isRegularFile(
                    languageFile)) {

                throw new OcrProcessingException(
                        "Tesseract language data "
                                + "is missing: "
                                + languageFile
                );
            }
        }
    }
}