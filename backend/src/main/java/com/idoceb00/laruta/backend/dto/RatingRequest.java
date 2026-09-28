package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(
        @Min(0) @Max(100)
        Integer rating
) {
}
