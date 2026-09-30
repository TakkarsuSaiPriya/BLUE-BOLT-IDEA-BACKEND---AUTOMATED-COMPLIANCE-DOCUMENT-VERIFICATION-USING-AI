package com.compliance.authservice.controller;

import com.compliance.authservice.audit.Auditable;
import com.compliance.authservice.dto.response.ApiResponse;
import com.compliance.authservice.dto.response.UserResponse;
import com.compliance.authservice.enums.AuditAction;
import com.compliance.authservice.service.interfaces.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService) {

        this.userService = userService;
    }

    @Auditable(action = AuditAction.USER_VIEWED)
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'AUDITOR', 'VERIFIER', 'USER')"
    )
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable Long id) {

        UserResponse response =
                userService.getUserById(id);

        return new ApiResponse<>(
                true,
                "User fetched successfully",
                response
        );
    }

    @Auditable(action = AuditAction.USERS_VIEWED)
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'AUDITOR', 'VERIFIER')"
    )
    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {

        List<UserResponse> response =
                userService.getAllUsers();

        return new ApiResponse<>(
                true,
                "Users fetched successfully",
                response
        );
    }

    @Auditable(action = AuditAction.USER_DELETED)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return new ApiResponse<>(
                true,
                "User deleted successfully",
                "Deleted"
        );
    }
}