package com.compliance.ocrservice.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AuthenticatedUser {

    private final String username;
    private final Set<String> roles;

    public AuthenticatedUser(
            String username,
            Set<String> roles) {

        this.username =
                normalizeUsername(username);

        this.roles =
                normalizeRoles(roles);
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean hasRole(
            String role) {

        if (role == null
                || role.isBlank()) {

            return false;
        }

        String normalizedRole =
                normalizeRole(role);

        return roles.contains(
                normalizedRole
        );
    }

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated username is required"
            );
        }

        return username.trim()
                .toLowerCase();
    }

    private Set<String> normalizeRoles(
            Set<String> suppliedRoles) {

        if (suppliedRoles == null
                || suppliedRoles.isEmpty()) {

            return Collections.emptySet();
        }

        Set<String> normalizedRoles =
                new LinkedHashSet<>();

        for (String role : suppliedRoles) {

            if (role == null
                    || role.isBlank()) {

                continue;
            }

            normalizedRoles.add(
                    normalizeRole(role)
            );
        }

        return Collections.unmodifiableSet(
                normalizedRoles
        );
    }

    private String normalizeRole(
            String role) {

        String normalizedRole =
                role.trim()
                        .toUpperCase();

        if (!normalizedRole.startsWith(
                "ROLE_")) {

            normalizedRole =
                    "ROLE_" + normalizedRole;
        }

        return normalizedRole;
    }
}