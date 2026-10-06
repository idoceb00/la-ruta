package com.idoceb00.laruta.backend.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
