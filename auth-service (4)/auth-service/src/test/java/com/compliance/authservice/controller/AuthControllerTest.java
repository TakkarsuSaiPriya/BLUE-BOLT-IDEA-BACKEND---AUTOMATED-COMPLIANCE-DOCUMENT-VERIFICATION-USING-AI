package com.compliance.authservice.controller;

import com.compliance.authservice.dto.request.LoginRequest;
import com.compliance.authservice.dto.request.RefreshTokenRequest;
import com.compliance.authservice.dto.request.RegisterRequest;
import com.compliance.authservice.dto.response.AuthResponse;
import com.compliance.authservice.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        AuthController authController =
                new AuthController(authService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void registerShouldReturnSuccessfulResponse()
            throws Exception {

        RegisterRequest request =
                new RegisterRequest();

        request.setFirstName("Sai");
        request.setLastName("Priya");
        request.setUsername("saipriya");
        request.setEmail("saipriya@example.com");
        request.setPassword("Password123");

        AuthResponse authResponse =
                createAuthResponse();

        when(authService.register(
                ArgumentMatchers.any(RegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "User registered successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.accessToken")
                                .value("access-token")
                )
                .andExpect(
                        jsonPath("$.data.refreshToken")
                                .value("refresh-token")
                )
                .andExpect(
                        jsonPath("$.data.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.username")
                                .value("saipriya")
                )
                .andExpect(
                        jsonPath("$.data.email")
                                .value(
                                        "saipriya@example.com"
                                )
                );

        verify(authService).register(
                ArgumentMatchers.any(RegisterRequest.class)
        );
    }

    @Test
    void loginShouldReturnSuccessfulResponse()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setUsername("saipriya");
        request.setPassword("Password123");

        AuthResponse authResponse =
                createAuthResponse();

        when(authService.login(
                ArgumentMatchers.any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Login successful")
                )
                .andExpect(
                        jsonPath("$.data.accessToken")
                                .value("access-token")
                )
                .andExpect(
                        jsonPath("$.data.refreshToken")
                                .value("refresh-token")
                )
                .andExpect(
                        jsonPath("$.data.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.data.username")
                                .value("saipriya")
                );

        verify(authService).login(
                ArgumentMatchers.any(LoginRequest.class)
        );
    }

    @Test
    void refreshTokenShouldReturnNewAccessToken()
            throws Exception {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("refresh-token");

        AuthResponse authResponse =
                createAuthResponse();

        when(authService.refreshToken(
                ArgumentMatchers.any(
                        RefreshTokenRequest.class
                )))
                .thenReturn(authResponse);

        mockMvc.perform(
                        post("/api/auth/refresh-token")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Access token refreshed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.accessToken")
                                .value("access-token")
                )
                .andExpect(
                        jsonPath("$.data.refreshToken")
                                .value("refresh-token")
                )
                .andExpect(
                        jsonPath("$.data.tokenType")
                                .value("Bearer")
                );

        verify(authService).refreshToken(
                ArgumentMatchers.any(
                        RefreshTokenRequest.class
                )
        );
    }

    private AuthResponse createAuthResponse() {

        return new AuthResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                1L,
                "saipriya",
                "saipriya@example.com"
        );
    }
}