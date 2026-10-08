package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.TapaReview;

public record TapaReviewResponse(
        Long barId,
        Long tapaId,
        String notes,
        Integer userRating,
        Double averageRating,
        long ratingCount,
        boolean fav
){
    public static TapaReviewResponse from(TapaReview tapaReview, Double averageRating, long ratingCount) {
        return new TapaReviewResponse(
            tapaReview.getBarTapa().getBar().getId(),
            tapaReview.getBarTapa().getTapa().getId(),
            tapaReview.getNotes(),
            tapaReview.getRating(),
            averageRating,
            ratingCount,
            tapaReview.isFav()
        );
    }
}
