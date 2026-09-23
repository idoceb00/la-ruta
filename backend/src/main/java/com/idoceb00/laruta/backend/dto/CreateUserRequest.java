package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "A username is required")
        @Size(min = 3, max = 30, message = "The username must be between 3 and 30 characters long")
        String username,

        @NotBlank(message = "A password is required")
        @Size(min = 4, max = 72, message = "The password must be between 4 and 72 characters long")
        String password
) {
}
