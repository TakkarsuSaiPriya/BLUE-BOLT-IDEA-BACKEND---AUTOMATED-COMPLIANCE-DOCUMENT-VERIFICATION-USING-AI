package com.compliance.authservice.service.interfaces;

import com.compliance.authservice.dto.request.LoginRequest;
import com.compliance.authservice.dto.request.RefreshTokenRequest;
import com.compliance.authservice.dto.request.RegisterRequest;
import com.compliance.authservice.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}