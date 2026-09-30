package com.compliance.authservice.service;

import com.compliance.authservice.entity.RefreshToken;
import com.compliance.authservice.entity.User;
import com.compliance.authservice.exception.RefreshTokenException;
import com.compliance.authservice.repository.RefreshTokenRepository;
import com.compliance.authservice.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {

        refreshTokenService =
                new RefreshTokenServiceImpl(
                        refreshTokenRepository
                );

        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenExpiration",
                604800000L
        );
    }

    @Test
    void createRefreshTokenShouldSaveNewToken() {

        User user = createUser();

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.empty());

        when(refreshTokenRepository.save(
                any(RefreshToken.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RefreshToken result =
                refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertSame(user, result.getUser());

        assertTrue(
                result.getExpiryDate()
                        .isAfter(LocalDateTime.now())
        );

        verify(refreshTokenRepository)
                .findByUser(user);

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    void createRefreshTokenShouldUpdateExistingToken() {

        User user = createUser();

        RefreshToken existingToken =
                new RefreshToken();

        existingToken.setId(5L);
        existingToken.setToken("old-refresh-token");
        existingToken.setExpiryDate(
                LocalDateTime.now().plusDays(1)
        );
        existingToken.setUser(user);

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(existingToken));

        when(refreshTokenRepository.save(existingToken))
                .thenReturn(existingToken);

        RefreshToken result =
                refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertEquals(5L, result.getId());

        assertNotEquals(
                "old-refresh-token",
                result.getToken()
        );

        assertNotNull(result.getExpiryDate());
        assertSame(user, result.getUser());

        verify(refreshTokenRepository)
                .findByUser(user);

        verify(refreshTokenRepository)
                .save(existingToken);
    }

    @Test
    void verifyRefreshTokenShouldReturnValidToken() {

        User user = createUser();

        RefreshToken validToken =
                new RefreshToken();

        validToken.setId(1L);
        validToken.setToken("valid-refresh-token");
        validToken.setExpiryDate(
                LocalDateTime.now().plusDays(1)
        );
        validToken.setUser(user);

        when(refreshTokenRepository.findByToken(
                "valid-refresh-token"))
                .thenReturn(Optional.of(validToken));

        RefreshToken result =
                refreshTokenService.verifyRefreshToken(
                        "valid-refresh-token"
                );

        assertNotNull(result);

        assertEquals(
                "valid-refresh-token",
                result.getToken()
        );

        assertSame(user, result.getUser());

        verify(refreshTokenRepository)
                .findByToken("valid-refresh-token");

        verify(refreshTokenRepository, never())
                .delete(any(RefreshToken.class));
    }

    @Test
    void verifyRefreshTokenShouldRejectInvalidToken() {

        when(refreshTokenRepository.findByToken(
                "invalid-refresh-token"))
                .thenReturn(Optional.empty());

        RefreshTokenException exception =
                assertThrows(
                        RefreshTokenException.class,
                        () -> refreshTokenService
                                .verifyRefreshToken(
                                        "invalid-refresh-token"
                                )
                );

        assertEquals(
                "Refresh token is invalid",
                exception.getMessage()
        );

        verify(refreshTokenRepository)
                .findByToken("invalid-refresh-token");

        verify(refreshTokenRepository, never())
                .delete(any(RefreshToken.class));
    }

    @Test
    void verifyRefreshTokenShouldDeleteExpiredToken() {

        User user = createUser();

        RefreshToken expiredToken =
                new RefreshToken();

        expiredToken.setId(1L);
        expiredToken.setToken("expired-refresh-token");
        expiredToken.setExpiryDate(
                LocalDateTime.now().minusMinutes(1)
        );
        expiredToken.setUser(user);

        when(refreshTokenRepository.findByToken(
                "expired-refresh-token"))
                .thenReturn(Optional.of(expiredToken));

        RefreshTokenException exception =
                assertThrows(
                        RefreshTokenException.class,
                        () -> refreshTokenService
                                .verifyRefreshToken(
                                        "expired-refresh-token"
                                )
                );

        assertEquals(
                "Refresh token has expired. Please log in again",
                exception.getMessage()
        );

        verify(refreshTokenRepository)
                .findByToken("expired-refresh-token");

        verify(refreshTokenRepository)
                .delete(expiredToken);
    }

    @Test
    void deleteByUserShouldDeleteRefreshToken() {

        User user = createUser();

        refreshTokenService.deleteByUser(user);

        verify(refreshTokenRepository)
                .deleteByUser(user);
    }

    private User createUser() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("Sai");
        user.setLastName("Priya");
        user.setUsername("saipriya");
        user.setEmail("saipriya@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        return user;
    }
}