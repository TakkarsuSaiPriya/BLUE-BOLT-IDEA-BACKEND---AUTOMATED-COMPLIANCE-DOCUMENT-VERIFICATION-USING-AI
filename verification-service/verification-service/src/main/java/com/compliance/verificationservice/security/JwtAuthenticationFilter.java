package com.compliance.verificationservice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    JwtAuthenticationFilter.class
            );

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService =
                jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                BEARER_PREFIX)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader.substring(
                                BEARER_PREFIX.length()
                        )
                        .trim();

        if (token.isBlank()) {

            request.setAttribute(
                    "authenticationError",
                    "Bearer token is empty"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null
                    && jwtService.isTokenValid(
                    token)) {

                String username =
                        jwtService.extractUsername(
                                token
                        );

                Set<String> roles =
                        jwtService.extractRoles(
                                token
                        );

                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                                .map(
                                        SimpleGrantedAuthority::new
                                )
                                .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        request.getRemoteAddr()
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }

        } catch (JwtException
                 | IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();

            request.setAttribute(
                    "authenticationError",
                    safeMessage(
                            exception
                    )
            );

            LOGGER.debug(
                    "JWT authentication failed for request {}: {}",
                    request.getRequestURI(),
                    exception.getMessage()
            );
        }

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

        return path.startsWith(
                "/actuator"
        )
                || path.startsWith(
                "/swagger-ui"
        )
                || path.startsWith(
                "/v3/api-docs"
        )
                || path.equals(
                "/swagger-ui.html"
        )
                || path.startsWith(
                "/api/internal/"
        );
    }

    private String safeMessage(
            Exception exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return "Invalid or expired JWT token";
        }

        String message =
                exception.getMessage()
                        .trim();

        if (message.length() > 500) {
            return message.substring(
                    0,
                    500
            );
        }

        return message;
    }
}