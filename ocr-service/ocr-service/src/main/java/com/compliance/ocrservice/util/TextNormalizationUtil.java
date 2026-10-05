package com.compliance.ocrservice.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class TextNormalizationUtil {

    public String normalize(
            String extractedText) {

        if (extractedText == null
                || extractedText.isBlank()) {

            return "";
        }

        String normalizedText =
                Normalizer.normalize(
                        extractedText,
                        Normalizer.Form.NFKC
                );

        normalizedText =
                normalizedText
                        .replace("\r\n", "\n")
                        .replace('\r', '\n')
                        .replace('\u00A0', ' ')
                        .replace('\u0000', ' ');

        normalizedText =
                Arrays.stream(
                                normalizedText.split(
                                        "\n",
                                        -1
                                )
                        )
                        .map(this::normalizeLine)
                        .collect(
                                Collectors.joining("\n")
                        );

        normalizedText =
                normalizedText.replaceAll(
                        "\n{3,}",
                        "\n\n"
                );

        return normalizedText.trim();
    }

    public int countWords(
            String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim()
                .split("\\s+")
                .length;
    }

    public int countCharacters(
            String text) {

        if (text == null) {
            return 0;
        }

        return text.length();
    }

    private String normalizeLine(
            String line) {

        if (line == null) {
            return "";
        }

        return line
                .replaceAll("[\\t ]+", " ")
                .strip();
    }
}