package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BarRatingRequest(
        @NotNull(message = "A rating value is required")
        @Min(value = 0, message = "Rating must be between 0 and 10") @Max(value = 10, message = "Rating must be between 0 and 10")
        Integer rating
) {
}
