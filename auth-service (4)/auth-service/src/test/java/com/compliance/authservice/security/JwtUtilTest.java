package com.compliance.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {

        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtSecret",
                "4f6e655365637265744b6579466f72417574685365727669636532303236"
        );

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtExpiration",
                900000L
        );
    }

    @Test
    void generateTokenShouldCreateValidToken() {

        String token =
                jwtUtil.generateToken("saipriya");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void getUsernameFromTokenShouldReturnUsername() {

        String token =
                jwtUtil.generateToken("saipriya");

        String username =
                jwtUtil.getUsernameFromToken(token);

        assertEquals("saipriya", username);
    }

    @Test
    void validateTokenShouldRejectInvalidToken() {

        boolean valid =
                jwtUtil.validateToken(
                        "invalid.jwt.token"
                );

        assertFalse(valid);
    }

    @Test
    void validateTokenShouldRejectExpiredToken() {

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtExpiration",
                -1000L
        );

        String expiredToken =
                jwtUtil.generateToken("saipriya");

        assertFalse(
                jwtUtil.validateToken(expiredToken)
        );
    }
}