package com.compliance.verificationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InternalServiceKeyFilter
        extends OncePerRequestFilter {

    private static final String SERVICE_KEY_HEADER =
            "X-Service-Key";

    private static final String INTERNAL_PATH_PREFIX =
            "/api/internal/";

    private final byte[] expectedServiceKey;

    private final ObjectMapper objectMapper;

    public InternalServiceKeyFilter(
            @Value("${internal.service-key}")
            String internalServiceKey,
            ObjectMapper objectMapper) {

        if (internalServiceKey == null
                || internalServiceKey.isBlank()) {

            throw new IllegalStateException(
                    "Internal service key must be configured"
            );
        }

        this.expectedServiceKey =
                internalServiceKey.trim()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        this.objectMapper =
                objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String providedServiceKey =
                request.getHeader(
                        SERVICE_KEY_HEADER
                );

        if (!matchesExpectedKey(
                providedServiceKey)) {

            writeUnauthorizedResponse(
                    request,
                    response
            );

            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_INTERNAL_SERVICE"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        filterChain.doFilter(
                request,
                response
        );
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path =
                request.getRequestURI();

        return path == null
                || !path.startsWith(
                INTERNAL_PATH_PREFIX
        );
    }

    private boolean matchesExpectedKey(
            String providedServiceKey) {

        if (providedServiceKey == null
                || providedServiceKey.isBlank()) {

            return false;
        }

        byte[] providedKey =
                providedServiceKey.trim()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        return MessageDigest.isEqual(
                expectedServiceKey,
                providedKey
        );
    }

    private void writeUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

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
                "A valid internal service key is required"
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