package com.compliance.ocrservice.security;

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

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    JwtAuthenticationFilter.class
            );

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
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
                BEARER_PREFIX
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader
                        .substring(
                                BEARER_PREFIX.length()
                        )
                        .trim();

        if (token.isBlank()) {
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
                    && jwtUtil.validateToken(token)) {

                AuthenticatedUser authenticatedUser =
                        jwtUtil.getAuthenticatedUser(
                                token
                        );

                List<SimpleGrantedAuthority> authorities =
                        authenticatedUser
                                .getRoles()
                                .stream()
                                .map(
                                        SimpleGrantedAuthority::new
                                )
                                .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
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

            LOGGER.debug(
                    "JWT authentication failed: {}",
                    exception.getMessage()
            );

            SecurityContextHolder
                    .clearContext();
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
                request.getServletPath();

        if (path == null) {
            return false;
        }

        return path.equals(
                "/actuator/health"
        )
                || path.equals(
                "/actuator/info"
        )
                || path.equals(
                "/swagger-ui.html"
        )
                || path.startsWith(
                "/swagger-ui/"
        )
                || path.startsWith(
                "/v3/api-docs"
        );
    }
}