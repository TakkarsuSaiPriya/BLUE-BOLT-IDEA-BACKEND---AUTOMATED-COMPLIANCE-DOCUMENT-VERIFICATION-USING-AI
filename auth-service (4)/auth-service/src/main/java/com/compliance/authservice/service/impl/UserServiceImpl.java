package com.compliance.authservice.service.impl;

import com.compliance.authservice.dto.response.UserResponse;
import com.compliance.authservice.entity.User;
import com.compliance.authservice.exception.UserNotFoundException;
import com.compliance.authservice.mapper.UserMapper;
import com.compliance.authservice.repository.UserRepository;
import com.compliance.authservice.service.interfaces.RefreshTokenService;
import com.compliance.authservice.service.interfaces.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found with ID: " + id
                                )
                        );

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found with ID: " + id
                                )
                        );

        refreshTokenService.deleteByUser(user);
        userRepository.delete(user);
    }
}