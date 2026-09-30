package com.compliance.documentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public boolean validateToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            parseClaims(token);
            return true;

        } catch (JwtException exception) {
            return false;

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {

        return parseClaims(token)
                .getSubject();
    }

    public Set<String> getRolesFromToken(String token) {

        Claims claims = parseClaims(token);

        Object rolesClaim =
                claims.get("roles");

        if (rolesClaim == null) {

            Object authoritiesClaim =
                    claims.get("authorities");

            rolesClaim = authoritiesClaim;
        }

        if (rolesClaim == null) {
            return Collections.emptySet();
        }

        if (rolesClaim instanceof Collection<?> collection) {

            Set<String> roles =
                    new LinkedHashSet<>();

            for (Object value : collection) {

                if (value != null
                        && !value.toString().isBlank()) {

                    roles.add(
                            normalizeRole(
                                    value.toString()
                            )
                    );
                }
            }

            return roles;
        }

        if (rolesClaim instanceof String roleText) {

            if (roleText.isBlank()) {
                return Collections.emptySet();
            }

            String cleanedRoleText =
                    roleText
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "");

            Set<String> roles =
                    new LinkedHashSet<>();

            String[] values =
                    cleanedRoleText.split(",");

            for (String value : values) {

                if (!value.isBlank()) {
                    roles.add(
                            normalizeRole(value)
                    );
                }
            }

            return roles;
        }

        return Collections.emptySet();
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String normalizeRole(String role) {

        String normalizedRole =
                role.trim()
                        .toUpperCase(Locale.ROOT);

        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole;
        }

        return "ROLE_" + normalizedRole;
    }
}