package com.idoceb00.laruta.backend.dto;

public record BarRatingStats(
        Long barId,
        Double averageRating,
        Long ratingCount
) {
}
