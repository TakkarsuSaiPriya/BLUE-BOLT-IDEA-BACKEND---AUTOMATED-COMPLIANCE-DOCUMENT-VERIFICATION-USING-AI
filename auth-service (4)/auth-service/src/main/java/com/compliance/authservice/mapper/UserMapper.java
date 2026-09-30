package com.compliance.authservice.mapper;

import com.compliance.authservice.dto.response.UserResponse;
import com.compliance.authservice.entity.Role;
import com.compliance.authservice.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(convertRoles(user.getRoles()));

        return response;
    }

    private Set<String> convertRoles(Set<Role> roles) {

        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}