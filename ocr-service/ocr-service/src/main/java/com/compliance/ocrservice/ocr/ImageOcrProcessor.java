package com.compliance.ocrservice.ocr;

import com.compliance.ocrservice.exception.OcrProcessingException;
import com.compliance.ocrservice.util.DocumentContentUtil;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class ImageOcrProcessor
        implements OcrProcessor {

    private final TesseractOcrEngine
            tesseractOcrEngine;

    private final DocumentContentUtil
            documentContentUtil;

    public ImageOcrProcessor(
            TesseractOcrEngine tesseractOcrEngine,
            DocumentContentUtil documentContentUtil) {

        this.tesseractOcrEngine =
                tesseractOcrEngine;

        this.documentContentUtil =
                documentContentUtil;
    }

    @Override
    public boolean supports(
            String contentType) {

        return documentContentUtil.isImage(
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
                determineContentType(
                        documentContent
                ),
                originalFileName
        );

        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(
                             documentContent
                     )) {

            BufferedImage image =
                    ImageIO.read(inputStream);

            if (image == null) {
                throw new OcrProcessingException(
                        "Unable to decode the "
                                + "document image"
                );
            }

            return tesseractOcrEngine.recognize(
                    image,
                    language
            );

        } catch (IOException exception) {

            throw new OcrProcessingException(
                    "Unable to read the "
                            + "document image",
                    exception
            );
        }
    }

    private String determineContentType(
            byte[] content) {

        if (content != null
                && content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47) {

            return "image/png";
        }

        if (content != null
                && content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF) {

            return "image/jpeg";
        }

        throw new OcrProcessingException(
                "Unsupported or invalid document image"
        );
    }
}