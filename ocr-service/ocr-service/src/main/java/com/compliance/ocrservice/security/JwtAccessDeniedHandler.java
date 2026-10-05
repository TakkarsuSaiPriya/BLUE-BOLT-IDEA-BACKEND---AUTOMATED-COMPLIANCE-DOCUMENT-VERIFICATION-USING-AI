package com.compliance.ocrservice.security;

import com.compliance.ocrservice.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        ErrorResponse errorResponse =
                new ErrorResponse();

        errorResponse.setSuccess(false);

        errorResponse.setMessage(
                "You do not have permission "
                        + "to access this resource"
        );

        errorResponse.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        errorResponse.setPath(
                request.getRequestURI()
        );

        errorResponse.setTimestamp(
                LocalDateTime.now()
        );

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
