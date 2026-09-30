package com.compliance.authservice.controller;

import com.compliance.authservice.audit.Auditable;
import com.compliance.authservice.dto.request.LoginRequest;
import com.compliance.authservice.dto.request.RefreshTokenRequest;
import com.compliance.authservice.dto.request.RegisterRequest;
import com.compliance.authservice.dto.response.ApiResponse;
import com.compliance.authservice.dto.response.AuthResponse;
import com.compliance.authservice.enums.AuditAction;
import com.compliance.authservice.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Auditable(action = AuditAction.USER_REGISTERED)
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse =
                authService.register(request);

        return new ApiResponse<>(
                true,
                "User registered successfully",
                authResponse
        );
    }

    @Auditable(action = AuditAction.USER_LOGGED_IN)
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse =
                authService.login(request);

        return new ApiResponse<>(
                true,
                "Login successful",
                authResponse
        );
    }

    @Auditable(action = AuditAction.TOKEN_REFRESHED)
    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse authResponse =
                authService.refreshToken(request);

        return new ApiResponse<>(
                true,
                "Access token refreshed successfully",
                authResponse
        );
    }
}