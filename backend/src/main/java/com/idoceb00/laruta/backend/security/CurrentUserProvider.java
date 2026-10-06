package com.idoceb00.laruta.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {

    public Optional<Long> findCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                // Public endpoints have an anonymous principal (a String), not a Jwt
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(jwt -> jwt.<Number>getClaim("userId"))
                .map(Number::longValue);
    }

    // For services behind authenticated endpoints: a missing user here is a configuration bug
    public Long getCurrentUserId() {
        return findCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in security context"));
    }
}