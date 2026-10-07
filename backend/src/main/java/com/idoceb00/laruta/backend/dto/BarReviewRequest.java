package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record BarReviewRequest(
        @Size(max = 1000, message = "The notes must be at most 1000 characters long")
        String notes,

        @Min(value = 0, message = "Rating must be between 0 and 10") @Max(value = 10, message = "Rating must be between 0 and 10")
        Integer rating
) {

        @AssertTrue(message = "A review must have a rating or notes")
        public boolean isNotEmpty() {
            return rating != null || (notes != null && !notes.isBlank());
        }
}
