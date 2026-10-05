package com.compliance.ocrservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalServiceFeignConfig {

    private static final String SERVICE_KEY_HEADER =
            "X-Service-Key";

    @Bean
    public RequestInterceptor internalServiceKeyInterceptor(
            @Value("${internal.service-key}")
            String serviceKey) {

        if (serviceKey == null
                || serviceKey.isBlank()) {

            throw new IllegalStateException(
                    "Internal service key must be configured"
            );
        }

        return requestTemplate ->
                requestTemplate.header(
                        SERVICE_KEY_HEADER,
                        serviceKey
                );
    }
}