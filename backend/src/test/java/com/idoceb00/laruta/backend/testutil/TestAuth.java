package com.idoceb00.laruta.backend.testutil;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// Shared helpers to authenticate MockMvc requests in integration tests
public final class TestAuth {

    private TestAuth() {
    }

    // Simulates an already validated JWT carrying the userId claim
    public static RequestPostProcessor authenticatedAs(Long userId) {
        return jwt().jwt(jwt -> jwt.claim("userId", userId));
    }
}