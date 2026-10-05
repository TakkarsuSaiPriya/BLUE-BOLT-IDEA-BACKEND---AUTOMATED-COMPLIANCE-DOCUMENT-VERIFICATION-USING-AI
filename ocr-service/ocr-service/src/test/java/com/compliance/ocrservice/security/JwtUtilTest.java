package com.compliance.ocrservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String JWT_SECRET =
            "4f6e655365637265744b6579466f72417574685365727669636532303236";

    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {

        jwtUtil =
                new JwtUtil(
                        JWT_SECRET
                );

        signingKey =
                Keys.hmacShaKeyFor(
                        JWT_SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {

        String token =
                createToken(
                        "testuser",
                        List.of(
                                "ROLE_USER"
                        ),
                        new Date(
                                System.currentTimeMillis()
                                        + 60000L
                        )
                );

        assertTrue(
                jwtUtil.validateToken(token)
        );
    }

    @Test
    void validateTokenShouldReturnFalseForExpiredToken() {

        String token =
                createToken(
                        "testuser",
                        List.of(
                                "ROLE_USER"
                        ),
                        new Date(
                                System.currentTimeMillis()
                                        - 60000L
                        )
                );

        assertFalse(
                jwtUtil.validateToken(token)
        );
    }

    @Test
    void validateTokenShouldReturnFalseForBlankToken() {

        assertFalse(
                jwtUtil.validateToken(null)
        );

        assertFalse(
                jwtUtil.validateToken("")
        );

        assertFalse(
                jwtUtil.validateToken("   ")
        );
    }

    @Test
    void validateTokenShouldReturnFalseForMalformedToken() {

        assertFalse(
                jwtUtil.validateToken(
                        "this-is-not-a-valid-jwt"
                )
        );
    }

    @Test
    void getUsernameFromTokenShouldNormalizeUsername() {

        String token =
                createToken(
                        "TestUser",
                        List.of(
                                "ROLE_USER"
                        ),
                        new Date(
                                System.currentTimeMillis()
                                        + 60000L
                        )
                );

        String username =
                jwtUtil.getUsernameFromToken(
                        token
                );

        assertEquals(
                "testuser",
                username
        );
    }

    @Test
    void getRolesFromTokenShouldReadCollectionClaim() {

        String token =
                createToken(
                        "testuser",
                        List.of(
                                "ROLE_USER",
                                "ADMIN"
                        ),
                        new Date(
                                System.currentTimeMillis()
                                        + 60000L
                        )
                );

        Set<String> roles =
                jwtUtil.getRolesFromToken(
                        token
                );

        assertTrue(
                roles.contains(
                        "ROLE_USER"
                )
        );

        assertTrue(
                roles.contains(
                        "ROLE_ADMIN"
                )
        );
    }

    @Test
    void getRolesFromTokenShouldReadSingleRoleClaim() {

        String token =
                Jwts.builder()
                        .subject("testuser")
                        .claim(
                                "role",
                                "VERIFIER"
                        )
                        .issuedAt(
                                new Date()
                        )
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + 60000L
                                )
                        )
                        .signWith(signingKey)
                        .compact();

        Set<String> roles =
                jwtUtil.getRolesFromToken(
                        token
                );

        assertEquals(
                Set.of(
                        "ROLE_VERIFIER"
                ),
                roles
        );
    }

    @Test
    void getAuthenticatedUserShouldCreatePrincipal() {

        String token =
                createToken(
                        "AuditUser",
                        List.of(
                                "AUDITOR"
                        ),
                        new Date(
                                System.currentTimeMillis()
                                        + 60000L
                        )
                );

        AuthenticatedUser authenticatedUser =
                jwtUtil.getAuthenticatedUser(
                        token
                );

        assertEquals(
                "audituser",
                authenticatedUser.getUsername()
        );

        assertTrue(
                authenticatedUser.hasRole(
                        "AUDITOR"
                )
        );
    }

    @Test
    void constructorShouldRejectShortSecret() {

        assertThrows(
                IllegalStateException.class,
                () -> new JwtUtil(
                        "short-secret"
                )
        );
    }

    private String createToken(
            String username,
            List<String> roles,
            Date expiration) {

        return Jwts.builder()
                .subject(username)
                .claim(
                        "roles",
                        roles
                )
                .issuedAt(
                        new Date()
                )
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}