package com.compliance.authservice.service.impl;

import com.compliance.authservice.entity.RefreshToken;
import com.compliance.authservice.entity.User;
import com.compliance.authservice.exception.RefreshTokenException;
import com.compliance.authservice.repository.RefreshTokenRepository;
import com.compliance.authservice.service.interfaces.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository) {

        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByUser(user)
                        .orElseGet(RefreshToken::new);

        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000)
        );

        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RefreshTokenException(
                                        "Refresh token is invalid"
                                )
                        );

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new RefreshTokenException(
                    "Refresh token has expired. Please log in again"
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {

        refreshTokenRepository.deleteByUser(user);
    }
}