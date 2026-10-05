package com.compliance.verificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(
            @Value("${jwt.secret}")
            String jwtSecret) {

        this.signingKey =
                createSigningKey(
                        jwtSecret
                );
    }

    public Claims parseClaims(
            String token) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "JWT token is required"
            );
        }

        return Jwts.parser()
                .verifyWith(
                        signingKey
                )
                .build()
                .parseSignedClaims(
                        token.trim()
                )
                .getPayload();
    }

    public boolean isTokenValid(
            String token) {

        try {

            Claims claims =
                    parseClaims(
                            token
                    );

            return claims.getSubject() != null
                    && !claims.getSubject().isBlank()
                    && claims.getExpiration() != null
                    && claims.getExpiration()
                    .getTime() > System.currentTimeMillis();

        } catch (JwtException
                 | IllegalArgumentException exception) {

            return false;
        }
    }

    public String extractUsername(
            String token) {

        Claims claims =
                parseClaims(
                        token
                );

        String username =
                claims.getSubject();

        if (username == null
                || username.isBlank()) {

            username =
                    firstNonBlankClaim(
                            claims,
                            "username",
                            "preferred_username",
                            "userName",
                            "email"
                    );
        }

        if (username == null
                || username.isBlank()) {

            throw new JwtException(
                    "JWT does not contain a valid username"
            );
        }

        return normalizeUsername(
                username
        );
    }

    public Set<String> extractRoles(
            String token) {

        Claims claims =
                parseClaims(
                        token
                );

        Set<String> roles =
                new LinkedHashSet<>();

        collectRoles(
                roles,
                claims.get("roles")
        );

        collectRoles(
                roles,
                claims.get("role")
        );

        collectRoles(
                roles,
                claims.get("authorities")
        );

        collectRoles(
                roles,
                claims.get("scope")
        );

        collectRoles(
                roles,
                claims.get("scp")
        );

        collectNestedRoles(
                roles,
                claims.get("realm_access")
        );

        if (roles.isEmpty()) {
            roles.add(
                    "ROLE_USER"
            );
        }

        return roles;
    }

    private SecretKey createSigningKey(
            String jwtSecret) {

        if (jwtSecret == null
                || jwtSecret.isBlank()) {

            throw new IllegalStateException(
                    "JWT secret must be configured"
            );
        }

        String normalizedSecret =
                jwtSecret.trim();

        byte[] secretBytes;

        if (isHexadecimalSecret(
                normalizedSecret)) {

            secretBytes =
                    decodeHex(
                            normalizedSecret
                    );

        } else {

            secretBytes =
                    normalizedSecret.getBytes(
                            StandardCharsets.UTF_8
                    );
        }

        if (secretBytes.length < 32) {

            throw new IllegalStateException(
                    "JWT secret must contain at least 256 bits"
            );
        }

        return Keys.hmacShaKeyFor(
                secretBytes
        );
    }

    private boolean isHexadecimalSecret(
            String value) {

        return value.length() % 2 == 0
                && value.matches(
                "^[0-9a-fA-F]+$"
        );
    }

    private byte[] decodeHex(
            String hexadecimalValue) {

        int length =
                hexadecimalValue.length();

        byte[] bytes =
                new byte[length / 2];

        for (int index = 0;
             index < length;
             index += 2) {

            int high =
                    Character.digit(
                            hexadecimalValue.charAt(
                                    index
                            ),
                            16
                    );

            int low =
                    Character.digit(
                            hexadecimalValue.charAt(
                                    index + 1
                            ),
                            16
                    );

            if (high < 0
                    || low < 0) {

                throw new IllegalStateException(
                        "JWT secret contains invalid hexadecimal characters"
                );
            }

            bytes[index / 2] =
                    (byte) ((high << 4) + low);
        }

        return bytes;
    }

    private String firstNonBlankClaim(
            Claims claims,
            String... claimNames) {

        for (String claimName : claimNames) {

            Object value =
                    claims.get(
                            claimName
                    );

            if (value != null
                    && !value.toString()
                    .isBlank()) {

                return value.toString();
            }
        }

        return null;
    }

    private void collectRoles(
            Set<String> destination,
            Object rawRoles) {

        if (rawRoles == null) {
            return;
        }

        if (rawRoles instanceof Collection<?> collection) {

            for (Object value : collection) {
                addRole(
                        destination,
                        value
                );
            }

            return;
        }

        if (rawRoles.getClass().isArray()) {

            Object[] values =
                    (Object[]) rawRoles;

            for (Object value : values) {
                addRole(
                        destination,
                        value
                );
            }

            return;
        }

        String text =
                rawRoles.toString();

        if (text.isBlank()) {
            return;
        }

        List<String> values =
                splitRoles(
                        text
                );

        for (String value : values) {
            addRole(
                    destination,
                    value
            );
        }
    }

    private void collectNestedRoles(
            Set<String> destination,
            Object nestedValue) {

        if (!(nestedValue instanceof Map<?, ?> map)) {
            return;
        }

        Object nestedRoles =
                map.get("roles");

        collectRoles(
                destination,
                nestedRoles
        );
    }

    private List<String> splitRoles(
            String rolesText) {

        String normalized =
                rolesText.replace(
                                "[",
                                ""
                        )
                        .replace(
                                "]",
                                ""
                        )
                        .replace(
                                "\"",
                                ""
                        )
                        .replace(
                                "'",
                                ""
                        );

        String[] candidates =
                normalized.split(
                        "[,\\s]+"
                );

        List<String> roles =
                new ArrayList<>();

        for (String candidate : candidates) {

            if (candidate != null
                    && !candidate.isBlank()) {

                roles.add(
                        candidate.trim()
                );
            }
        }

        return roles;
    }

    private void addRole(
            Set<String> destination,
            Object rawRole) {

        if (rawRole == null) {
            return;
        }

        String role =
                rawRole.toString()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (role.isBlank()) {
            return;
        }

        if (role.startsWith(
                "SCOPE_")) {

            destination.add(
                    role
            );

            return;
        }

        if (!role.startsWith(
                "ROLE_")) {

            role =
                    "ROLE_" + role;
        }

        destination.add(
                role
        );
    }

    private String normalizeUsername(
            String username) {

        String normalized =
                username.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (normalized.length() > 150) {
            return normalized.substring(
                    0,
                    150
            );
        }

        return normalized;
    }
}