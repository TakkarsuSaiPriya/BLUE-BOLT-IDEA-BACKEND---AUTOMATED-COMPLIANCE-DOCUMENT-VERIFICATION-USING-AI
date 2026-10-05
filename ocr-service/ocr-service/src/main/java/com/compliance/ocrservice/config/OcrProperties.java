package com.compliance.ocrservice.config;

import com.compliance.ocrservice.enums.OcrEngine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "ocr")
public class OcrProperties {

    private String language = "eng";

    private String dataPath = "tessdata";

    private OcrEngine engine =
            OcrEngine.TESSERACT;

    private int pageSegmentationMode = 3;

    private int engineMode = 1;

    private int renderDpi = 300;

    private int maximumPages = 25;

    private double minimumConfidence = 40.0;

    private String temporaryDirectory =
            "ocr-temp";

    public OcrProperties() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(
            String language) {

        this.language = language;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(
            String dataPath) {

        this.dataPath = dataPath;
    }

    public OcrEngine getEngine() {
        return engine;
    }

    public void setEngine(
            OcrEngine engine) {

        this.engine = engine;
    }

    public int getPageSegmentationMode() {
        return pageSegmentationMode;
    }

    public void setPageSegmentationMode(
            int pageSegmentationMode) {

        this.pageSegmentationMode =
                pageSegmentationMode;
    }

    public int getEngineMode() {
        return engineMode;
    }

    public void setEngineMode(
            int engineMode) {

        this.engineMode = engineMode;
    }

    public int getRenderDpi() {
        return renderDpi;
    }

    public void setRenderDpi(
            int renderDpi) {

        this.renderDpi = renderDpi;
    }

    public int getMaximumPages() {
        return maximumPages;
    }

    public void setMaximumPages(
            int maximumPages) {

        this.maximumPages = maximumPages;
    }

    public double getMinimumConfidence() {
        return minimumConfidence;
    }

    public void setMinimumConfidence(
            double minimumConfidence) {

        this.minimumConfidence =
                minimumConfidence;
    }

    public String getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public void setTemporaryDirectory(
            String temporaryDirectory) {

        this.temporaryDirectory =
                temporaryDirectory;
    }
}