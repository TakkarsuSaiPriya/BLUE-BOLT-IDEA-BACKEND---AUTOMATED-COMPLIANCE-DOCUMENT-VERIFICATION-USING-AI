package com.compliance.verificationservice.config;

import com.compliance.verificationservice.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.nio.charset.StandardCharsets;

public class InternalFeignErrorDecoder
        implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder =
            new ErrorDecoder.Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response) {

        if (response == null) {

            return new ExternalServiceException(
                    resolveServiceName(methodKey),
                    "No response was received from the external service"
            );
        }

        int status =
                response.status();

        String serviceName =
                resolveServiceName(
                        methodKey
                );

        String responseBody =
                readResponseBody(
                        response
                );

        String message =
                buildMessage(
                        serviceName,
                        status,
                        responseBody
                );

        if (status >= 400
                && status < 600) {

            return new ExternalServiceException(
                    serviceName,
                    status,
                    message
            );
        }

        return defaultErrorDecoder.decode(
                methodKey,
                response
        );
    }

    private String resolveServiceName(
            String methodKey) {

        if (methodKey == null
                || methodKey.isBlank()) {

            return "external-service";
        }

        String normalizedMethodKey =
                methodKey.toLowerCase();

        if (normalizedMethodKey.contains(
                "ocrserviceclient")) {

            return "ocr-service";
        }

        if (normalizedMethodKey.contains(
                "documentserviceclient")) {

            return "document-service";
        }

        return "external-service";
    }

    private String readResponseBody(
            Response response) {

        if (response.body() == null) {
            return null;
        }

        try {

            byte[] bodyBytes =
                    response.body()
                            .asInputStream()
                            .readAllBytes();

            if (bodyBytes.length == 0) {
                return null;
            }

            return new String(
                    bodyBytes,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {

            return null;
        }
    }

    private String buildMessage(
            String serviceName,
            int status,
            String responseBody) {

        StringBuilder message =
                new StringBuilder();

        message.append(
                "Call to "
        );

        message.append(
                serviceName
        );

        message.append(
                " failed with HTTP status "
        );

        message.append(
                status
        );

        if (responseBody != null
                && !responseBody.isBlank()) {

            message.append(
                    ". Response: "
            );

            message.append(
                    limitText(
                            responseBody,
                            1000
                    )
            );
        }

        return message.toString();
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value == null) {
            return null;
        }

        String normalizedValue =
                value.trim();

        if (normalizedValue.length()
                > maximumLength) {

            return normalizedValue.substring(
                    0,
                    maximumLength
            );
        }

        return normalizedValue;
    }
}