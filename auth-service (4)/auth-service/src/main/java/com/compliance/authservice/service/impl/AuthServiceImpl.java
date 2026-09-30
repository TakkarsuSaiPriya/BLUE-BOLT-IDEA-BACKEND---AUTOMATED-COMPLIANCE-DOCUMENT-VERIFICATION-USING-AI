package com.compliance.authservice.service.impl;

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
import com.compliance.authservice.exception.ResourceNotFoundException;
import com.compliance.authservice.producer.AuthEventProducer;
import com.compliance.authservice.repository.RoleRepository;
import com.compliance.authservice.repository.UserRepository;
import com.compliance.authservice.security.JwtUtil;
import com.compliance.authservice.service.interfaces.AuthService;
import com.compliance.authservice.service.interfaces.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthEventProducer authEventProducer;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            AuthEventProducer authEventProducer) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.authEventProducer = authEventProducer;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String username =
                request.getUsername().trim().toLowerCase(Locale.ROOT);

        String email =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(
                    "Email address already exists"
            );
        }

        Role defaultRole =
                roleRepository.findByName(RoleType.ROLE_USER)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Default ROLE_USER is not configured"
                                )
                        );

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = new User();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        String accessToken =
                jwtUtil.generateToken(savedUser.getUsername());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        authEventProducer.publishUserRegistered(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );

        return createAuthResponse(
                savedUser,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String username =
                request.getUsername().trim().toLowerCase(Locale.ROOT);

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException exception) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );

        } catch (DisabledException exception) {

            throw new InvalidCredentialsException(
                    "User account is disabled"
            );

        } catch (LockedException exception) {

            throw new InvalidCredentialsException(
                    "User account is locked"
            );
        }

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid username or password"
                                )
                        );

        String accessToken =
                jwtUtil.generateToken(user.getUsername());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        authEventProducer.publishUserLoggedIn(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        return createAuthResponse(
                user,
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(
            RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = refreshToken.getUser();

        String newAccessToken =
                jwtUtil.generateToken(user.getUsername());

        authEventProducer.publishTokenRefreshed(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        return createAuthResponse(
                user,
                newAccessToken,
                refreshToken.getToken()
        );
    }

    private AuthResponse createAuthResponse(
            User user,
            String accessToken,
            String refreshToken) {

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}