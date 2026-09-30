package com.compliance.documentservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET =
            "4f6e655365637265744b6579466f724175"
                    + "74685365727669636532303236";

    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {

        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtSecret",
                SECRET
        );

        signingKey =
                Keys.hmacShaKeyFor(
                        SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    @Test
    void validateTokenShouldAcceptValidToken() {

        String token =
                createToken(
                        "saipriya",
                        List.of("ROLE_USER"),
                        900000L
                );

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void getUsernameFromTokenShouldReturnSubject() {

        String token =
                createToken(
                        "saipriya",
                        List.of("ROLE_USER"),
                        900000L
                );

        assertEquals(
                "saipriya",
                jwtUtil.getUsernameFromToken(token)
        );
    }

    @Test
    void getRolesFromTokenShouldReturnRoles() {

        String token =
                createToken(
                        "saipriya",
                        List.of(
                                "ROLE_USER",
                                "ROLE_ADMIN"
                        ),
                        900000L
                );

        Set<String> roles =
                jwtUtil.getRolesFromToken(token);

        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void getRolesFromTokenShouldNormalizeRoleNames() {

        String token =
                createToken(
                        "saipriya",
                        List.of("USER", "admin"),
                        900000L
                );

        Set<String> roles =
                jwtUtil.getRolesFromToken(token);

        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void validateTokenShouldRejectExpiredToken() {

        String token =
                createToken(
                        "saipriya",
                        List.of("ROLE_USER"),
                        -1000L
                );

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void validateTokenShouldRejectMalformedToken() {

        assertFalse(
                jwtUtil.validateToken(
                        "invalid.jwt.token"
                )
        );
    }

    @Test
    void validateTokenShouldRejectBlankToken() {

        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    private String createToken(
            String username,
            List<String> roles,
            long expirationMilliseconds) {

        Date now = new Date();

        Date expiration =
                new Date(
                        now.getTime()
                                + expirationMilliseconds
                );

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}