package com.idoceb00.laruta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBarRequest(
        @NotBlank(message = "A name is required")
        @Size(max = 100, message = "The name must be between 1 and 100 characters long")
        String name,

        @Size(max = 255, message = "The city must be at most 255 characters long")
        String city,

        @Size(max = 255, message = "The address must be at most 255 characters long")
        String address,

        @Size(max = 255, message = "The zone must at most 255 characters long")
        String zone,

        @Size(max = 1000, message = "The notes must at most 1000 characters long")
        String notes
) {
}
