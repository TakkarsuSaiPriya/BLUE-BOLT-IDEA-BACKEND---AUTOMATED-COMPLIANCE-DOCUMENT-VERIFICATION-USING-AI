package com.compliance.authservice.service;

import com.compliance.authservice.dto.request.LoginRequest;
import com.compliance.authservice.dto.request.RefreshTokenRequest;
import com.compliance.authservice.dto.request.RegisterRequest;
import com.compliance.authservice.dto.response.AuthResponse;
import com.compliance.authservice.entity.RefreshToken;
import com.compliance.authservice.entity.Role;
import com.compliance.authservice.entity.User;
import com.compliance.authservice.enums.RoleType;
import com.compliance.authservice.exception.DuplicateUserException;
import com.compliance.authservice.exception.InvalidCredentialsException;
import com.compliance.authservice.producer.AuthEventProducer;
import com.compliance.authservice.repository.RoleRepository;
import com.compliance.authservice.repository.UserRepository;
import com.compliance.authservice.security.JwtUtil;
import com.compliance.authservice.service.impl.AuthServiceImpl;
import com.compliance.authservice.service.interfaces.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthEventProducer authEventProducer;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {

        authService = new AuthServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtUtil,
                refreshTokenService,
                authEventProducer
        );
    }

    @Test
    void registerShouldCreateUserAndReturnTokens() {

        RegisterRequest request = createRegisterRequest();

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleType.ROLE_USER);
        userRole.setDescription("Standard user");

        User savedUser = createUser();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("refresh-token-value");
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );
        refreshToken.setUser(savedUser);

        when(userRepository.existsByUsername("saipriya"))
                .thenReturn(false);

        when(userRepository.existsByEmail(
                "saipriya@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleType.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtUtil.generateToken("saipriya"))
                .thenReturn("access-token-value");

        when(refreshTokenService.createRefreshToken(savedUser))
                .thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token-value", response.getAccessToken());
        assertEquals(
                "refresh-token-value",
                response.getRefreshToken()
        );
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("saipriya", response.getUsername());
        assertEquals(
                "saipriya@example.com",
                response.getEmail()
        );

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password123");

        verify(authEventProducer).publishUserRegistered(
                1L,
                "saipriya",
                "saipriya@example.com"
        );
    }

    @Test
    void registerShouldRejectDuplicateUsername() {

        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByUsername("saipriya"))
                .thenReturn(true);

        DuplicateUserException exception =
                assertThrows(
                        DuplicateUserException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(authEventProducer, never())
                .publishUserRegistered(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void registerShouldRejectDuplicateEmail() {

        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByUsername("saipriya"))
                .thenReturn(false);

        when(userRepository.existsByEmail(
                "saipriya@example.com"))
                .thenReturn(true);

        DuplicateUserException exception =
                assertThrows(
                        DuplicateUserException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Email address already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void loginShouldAuthenticateAndReturnTokens() {

        LoginRequest request = new LoginRequest();
        request.setUsername("saipriya");
        request.setPassword("Password123");

        User user = createUser();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("login-refresh-token");
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );
        refreshToken.setUser(user);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(
                        new UsernamePasswordAuthenticationToken(
                                "saipriya",
                                null
                        )
                );

        when(userRepository.findByUsername("saipriya"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("saipriya"))
                .thenReturn("login-access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(
                "login-access-token",
                response.getAccessToken()
        );
        assertEquals(
                "login-refresh-token",
                response.getRefreshToken()
        );
        assertEquals("Bearer", response.getTokenType());
        assertEquals("saipriya", response.getUsername());

        verify(authenticationManager)
                .authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                );

        verify(authEventProducer).publishUserLoggedIn(
                1L,
                "saipriya",
                "saipriya@example.com"
        );
    }

    @Test
    void loginShouldRejectInvalidCredentials() {

        LoginRequest request = new LoginRequest();
        request.setUsername("saipriya");
        request.setPassword("WrongPassword123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"
                        )
                );

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByUsername(any());

        verify(authEventProducer, never())
                .publishUserLoggedIn(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void refreshTokenShouldReturnNewAccessToken() {

        RefreshTokenRequest request =
                new RefreshTokenRequest();

        request.setRefreshToken("valid-refresh-token");

        User user = createUser();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );
        refreshToken.setUser(user);

        when(refreshTokenService.verifyRefreshToken(
                "valid-refresh-token"))
                .thenReturn(refreshToken);

        when(jwtUtil.generateToken("saipriya"))
                .thenReturn("new-access-token");

        AuthResponse response =
                authService.refreshToken(request);

        assertNotNull(response);
        assertEquals(
                "new-access-token",
                response.getAccessToken()
        );
        assertEquals(
                "valid-refresh-token",
                response.getRefreshToken()
        );
        assertEquals("Bearer", response.getTokenType());

        verify(authEventProducer).publishTokenRefreshed(
                1L,
                "saipriya",
                "saipriya@example.com"
        );
    }

    private RegisterRequest createRegisterRequest() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Sai");
        request.setLastName("Priya");
        request.setUsername("SaiPriya");
        request.setEmail("SaiPriya@Example.com");
        request.setPassword("Password123");

        return request;
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