package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.BarRating;

public record BarRatingResponse(
        Long barId,
        Integer userRating,
        Double averageRating,
        long ratingCount
) {
    public static BarRatingResponse from(BarRating barRating, Double averageRating, long ratingCount){
        return new BarRatingResponse(
                barRating.getBar().getId(),
                barRating.getRating(),
                averageRating,
                ratingCount
        );
    }
}
