package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record TapaReviewRequest(
        @Size(max = 1000, message = "The notes must be at most 1000 characters long")
        String notes,

        @Min(value = 0, message = "Rating must be between 0 and 5") @Max(value = 5, message = "Rating must be between 0 and 5")
        Integer rating,

        boolean fav
) {

    @AssertTrue(message = "A review must have a rating, notes or be marked as favourite")
    public boolean isNotEmpty() {
        return rating != null || fav || (notes != null && !notes.isBlank());
    }
}
