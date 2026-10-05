package com.compliance.verificationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(
            ObjectMapper objectMapper) {

        this.objectMapper =
                objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        Object authenticationError =
                request.getAttribute(
                        "authenticationError"
                );

        String message =
                authenticationError == null
                        ? "Authentication is required to access this resource"
                        : authenticationError.toString();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "success",
                false
        );

        body.put(
                "status",
                HttpServletResponse.SC_UNAUTHORIZED
        );

        body.put(
                "error",
                "Unauthorized"
        );

        body.put(
                "message",
                limitText(
                        message,
                        1000
                )
        );

        body.put(
                "path",
                request.getRequestURI()
        );

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        objectMapper.writeValue(
                response.getWriter(),
                body
        );
    }

    private String limitText(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return "Authentication failed";
        }

        String normalized =
                value.trim();

        if (normalized.length() > maximumLength) {
            return normalized.substring(
                    0,
                    maximumLength
            );
        }

        return normalized;
    }
}