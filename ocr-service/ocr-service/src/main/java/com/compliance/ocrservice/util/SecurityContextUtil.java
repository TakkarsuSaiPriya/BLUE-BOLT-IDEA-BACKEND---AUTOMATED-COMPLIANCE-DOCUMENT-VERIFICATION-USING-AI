package com.compliance.ocrservice.util;

import com.compliance.ocrservice.security.AuthenticatedUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class SecurityContextUtil {

    public String getCurrentUsername() {

        Authentication authentication =
                getRequiredAuthentication();

        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof AuthenticatedUser authenticatedUser) {

            return authenticatedUser
                    .getUsername();
        }

        String username =
                authentication.getName();

        if (username == null
                || username.isBlank()) {

            throw new IllegalStateException(
                    "Authenticated username is unavailable"
            );
        }

        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    public Set<String> getCurrentRoles() {

        Authentication authentication =
                getRequiredAuthentication();

        Set<String> roles =
                new LinkedHashSet<>();

        for (GrantedAuthority authority
                : authentication.getAuthorities()) {

            if (authority == null
                    || authority.getAuthority() == null
                    || authority.getAuthority().isBlank()) {

                continue;
            }

            String role =
                    authority.getAuthority()
                            .trim()
                            .toUpperCase(Locale.ROOT);

            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            roles.add(role);
        }

        return Set.copyOf(roles);
    }

    public AuthenticatedUser getCurrentUser() {

        return new AuthenticatedUser(
                getCurrentUsername(),
                getCurrentRoles()
        );
    }

    public boolean hasRole(
            String role) {

        if (role == null
                || role.isBlank()) {

            return false;
        }

        String normalizedRole =
                role.trim()
                        .toUpperCase(Locale.ROOT);

        if (!normalizedRole.startsWith(
                "ROLE_")) {

            normalizedRole =
                    "ROLE_" + normalizedRole;
        }

        return getCurrentRoles()
                .contains(normalizedRole);
    }

    private Authentication
    getRequiredAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "No authenticated user is available"
            );
        }

        return authentication;
    }
}