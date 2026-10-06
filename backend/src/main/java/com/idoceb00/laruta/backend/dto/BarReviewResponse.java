package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.BarReview;

public record BarReviewResponse(
        Long barId,
        Integer userRating,
        Double averageRating,
        long ratingCount
) {
    public static BarReviewResponse from(BarReview barReview, Double averageRating, long ratingCount){
        return new BarReviewResponse(
                barReview.getBar().getId(),
                barReview.getRating(),
                averageRating,
                ratingCount
        );
    }
}
