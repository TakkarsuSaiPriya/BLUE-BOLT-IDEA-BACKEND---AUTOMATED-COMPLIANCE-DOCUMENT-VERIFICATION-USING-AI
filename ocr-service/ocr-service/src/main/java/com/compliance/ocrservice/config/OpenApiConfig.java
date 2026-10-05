package com.compliance.ocrservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    public OpenAPI ocrServiceOpenApi() {

        SecurityScheme securityScheme =
                new SecurityScheme()
                        .name(
                                SECURITY_SCHEME_NAME
                        )
                        .type(
                                SecurityScheme.Type.HTTP
                        )
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "Enter the JWT access token "
                                        + "returned by Auth Service"
                        );

        Components components =
                new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                securityScheme
                        );

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList(
                                SECURITY_SCHEME_NAME
                        );

        Info apiInfo =
                new Info()
                        .title(
                                "Blue Bolt OCR Service API"
                        )
                        .version("1.0.0")
                        .description(
                                "OCR processing APIs for "
                                        + "Blue Bolt automated compliance "
                                        + "document verification"
                        )
                        .contact(
                                new Contact()
                                        .name(
                                                "Blue Bolt Development Team"
                                        )
                        );

        return new OpenAPI()
                .info(apiInfo)
                .components(components)
                .addSecurityItem(
                        securityRequirement
                );
    }
}