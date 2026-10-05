package com.compliance.verificationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(
            ObjectMapper objectMapper) {

        this.objectMapper =
                objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
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
                HttpServletResponse.SC_FORBIDDEN
        );

        body.put(
                "error",
                "Forbidden"
        );

        body.put(
                "message",
                "The authenticated user does not have permission "
                        + "to access this resource"
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
}