package com.compliance.authservice.service.interfaces;

import com.compliance.authservice.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);
}