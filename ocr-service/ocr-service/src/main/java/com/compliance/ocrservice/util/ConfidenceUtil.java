package com.compliance.ocrservice.util;

import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfidenceUtil {

    public double calculateAverageConfidence(
            List<Word> words) {

        if (words == null || words.isEmpty()) {
            return 0.0;
        }

        double confidenceTotal = 0.0;
        int validWordCount = 0;

        for (Word word : words) {

            if (word == null) {
                continue;
            }

            String text = word.getText();

            if (text == null || text.isBlank()) {
                continue;
            }

            float confidence =
                    word.getConfidence();

            if (confidence < 0.0F) {
                continue;
            }

            confidenceTotal += confidence;
            validWordCount++;
        }

        if (validWordCount == 0) {
            return 0.0;
        }

        double average =
                confidenceTotal / validWordCount;

        return roundToTwoDecimalPlaces(
                average
        );
    }

    public boolean isLowConfidence(
            double averageConfidence,
            double minimumConfidence) {

        return averageConfidence
                < minimumConfidence;
    }

    private double roundToTwoDecimalPlaces(
            double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }
}