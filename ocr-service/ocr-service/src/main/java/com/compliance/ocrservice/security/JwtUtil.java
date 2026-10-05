package com.compliance.ocrservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class JwtUtil {

    private static final String ROLES_CLAIM =
            "roles";

    private static final String ROLE_CLAIM =
            "role";

    private final SecretKey signingKey;

    public JwtUtil(
            @Value("${jwt.secret}")
            String jwtSecret) {

        this.signingKey =
                createSigningKey(jwtSecret);
    }

    public boolean validateToken(
            String token) {

        if (token == null
                || token.isBlank()) {

            return false;
        }

        try {
            Claims claims =
                    extractClaims(token);

            String username =
                    claims.getSubject();

            return username != null
                    && !username.isBlank();

        } catch (JwtException
                 | IllegalArgumentException exception) {

            return false;
        }
    }

    public String getUsernameFromToken(
            String token) {

        Claims claims =
                extractClaims(token);

        String username =
                claims.getSubject();

        if (username == null
                || username.isBlank()) {

            throw new JwtException(
                    "JWT subject is missing"
            );
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    public Set<String> getRolesFromToken(
            String token) {

        Claims claims =
                extractClaims(token);

        Set<String> roles =
                new LinkedHashSet<>();

        addRoles(
                roles,
                claims.get(ROLES_CLAIM)
        );

        addRoles(
                roles,
                claims.get(ROLE_CLAIM)
        );

        return Set.copyOf(roles);
    }

    public AuthenticatedUser getAuthenticatedUser(
            String token) {

        return new AuthenticatedUser(
                getUsernameFromToken(token),
                getRolesFromToken(token)
        );
    }

    private Claims extractClaims(
            String token) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "JWT token is required"
            );
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey createSigningKey(
            String jwtSecret) {

        if (jwtSecret == null
                || jwtSecret.isBlank()) {

            throw new IllegalStateException(
                    "JWT secret must be configured"
            );
        }

        byte[] secretBytes =
                jwtSecret.getBytes(
                        StandardCharsets.UTF_8
                );

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least "
                            + "32 bytes for HMAC-SHA security"
            );
        }

        return Keys.hmacShaKeyFor(
                secretBytes
        );
    }

    private void addRoles(
            Set<String> targetRoles,
            Object claimValue) {

        if (claimValue == null) {
            return;
        }

        if (claimValue
                instanceof Collection<?> collection) {

            for (Object element : collection) {
                addSingleRole(
                        targetRoles,
                        element
                );
            }

            return;
        }

        if (claimValue
                instanceof String roleString) {

            String[] splitRoles =
                    roleString.split(",");

            for (String role : splitRoles) {
                addSingleRole(
                        targetRoles,
                        role
                );
            }

            return;
        }

        addSingleRole(
                targetRoles,
                claimValue
        );
    }

    private void addSingleRole(
            Set<String> targetRoles,
            Object value) {

        if (value == null) {
            return;
        }

        String role =
                value.toString()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (role.isBlank()) {
            return;
        }

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        targetRoles.add(role);
    }
}