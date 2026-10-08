package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.*;

public record BarTapaRequest(
        @NotBlank(message = "A name is required")
        @Size(max = 100, message = "The name must be between 1 and 100 characters long")
        String tapaName
) {
}
