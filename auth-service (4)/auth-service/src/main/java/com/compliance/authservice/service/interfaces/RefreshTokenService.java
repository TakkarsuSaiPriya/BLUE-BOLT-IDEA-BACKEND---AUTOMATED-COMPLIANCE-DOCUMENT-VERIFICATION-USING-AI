package com.compliance.authservice.service.interfaces;

import com.compliance.authservice.entity.RefreshToken;
import com.compliance.authservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void deleteByUser(User user);
}