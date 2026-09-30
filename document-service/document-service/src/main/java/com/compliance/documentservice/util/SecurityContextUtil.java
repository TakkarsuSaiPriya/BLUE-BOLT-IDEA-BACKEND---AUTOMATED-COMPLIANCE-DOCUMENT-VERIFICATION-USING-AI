package com.compliance.documentservice.util;

import com.compliance.documentservice.exception.JwtAuthenticationException;
import com.compliance.documentservice.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityContextUtil {

    public String getCurrentUsername() {

        Authentication authentication =
                getAuthentication();

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser authenticatedUser) {

            String username =
                    authenticatedUser.getUsername();

            if (username == null
                    || username.isBlank()) {

                throw new JwtAuthenticationException(
                        "Authenticated username is missing"
                );
            }

            return username;
        }

        String username =
                authentication.getName();

        if (username == null
                || username.isBlank()
                || "anonymousUser".equals(username)) {

            throw new JwtAuthenticationException(
                    "Authenticated username is missing"
            );
        }

        return username;
    }

    public Set<String> getCurrentRoles() {

        Authentication authentication =
                getAuthentication();

        if (authentication.getAuthorities() == null
                || authentication.getAuthorities().isEmpty()) {

            return Collections.emptySet();
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasRole(String role) {

        if (role == null || role.isBlank()) {
            return false;
        }

        String normalizedRole =
                role.startsWith("ROLE_")
                        ? role
                        : "ROLE_" + role;

        return getCurrentRoles()
                .contains(normalizedRole);
    }

    public boolean isAuthenticated() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(
                authentication.getPrincipal()
        );
    }

    private Authentication getAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {

            throw new JwtAuthenticationException(
                    "Authentication is required"
            );
        }

        return authentication;
    }
}