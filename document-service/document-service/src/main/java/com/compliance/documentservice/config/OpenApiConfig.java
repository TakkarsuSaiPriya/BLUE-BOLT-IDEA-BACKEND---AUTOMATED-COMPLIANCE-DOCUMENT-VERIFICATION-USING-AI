package com.compliance.documentservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME =
            "Bearer Authentication";

    @Bean
    public OpenAPI documentServiceOpenApi() {

        SecurityScheme securityScheme =
                new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(
                                SecurityScheme.Type.HTTP
                        )
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "Enter the JWT access token generated "
                                        + "by the Auth Service"
                        );

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList(
                                SECURITY_SCHEME_NAME
                        );

        Components components =
                new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                securityScheme
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Document Service API"
                                )
                                .description(
                                        "Document upload, storage, "
                                                + "download, metadata, "
                                                + "status, and audit APIs"
                                )
                                .version("1.0.0")
                                .license(
                                        new License()
                                                .name(
                                                        "Internal Use"
                                                )
                                )
                )
                .components(components)
                .addSecurityItem(
                        securityRequirement
                );
    }
}