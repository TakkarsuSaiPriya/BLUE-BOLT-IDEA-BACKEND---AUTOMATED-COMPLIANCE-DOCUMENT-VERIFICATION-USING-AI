package com.compliance.verificationservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalServiceFeignConfig {

    private static final String SERVICE_KEY_HEADER =
            "X-Service-Key";

    @Bean
    public RequestInterceptor
    internalServiceKeyInterceptor(
            @Value("${internal.service-key}")
            String serviceKey) {

        if (serviceKey == null
                || serviceKey.isBlank()) {

            throw new IllegalStateException(
                    "Internal service key must be configured"
            );
        }

        String normalizedServiceKey =
                serviceKey.trim();

        return requestTemplate ->
                requestTemplate.header(
                        SERVICE_KEY_HEADER,
                        normalizedServiceKey
                );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {

        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder internalFeignErrorDecoder() {

        return new InternalFeignErrorDecoder();
    }
}