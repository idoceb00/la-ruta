package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinCommunityRequest(
        @NotBlank(message = "An invite code is required")
        String inviteCode
) {
}
