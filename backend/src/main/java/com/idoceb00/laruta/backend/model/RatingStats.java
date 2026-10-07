package com.idoceb00.laruta.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingStats {

    @Column(nullable = false)
    private long ratingSum = 0;

    @Column(nullable = false)
    private long ratingCount = 0;

    protected Double getAverageRating() {
        if (ratingCount == 0){
            return null;
        }

        double average = (double) ratingSum / ratingCount;
        return Math.round(average * 10) / 10.0;
    }

    // Single entry point for rating changes. Without an access modifier, it is only visible to classes in the same package
    protected void replaceRating(Integer oldRating, Integer newRating) {
        if (oldRating != null) {
            removeRating(oldRating);
        }
        if (newRating != null) {
            addRating(newRating);
        }
    }

    private void addRating(int rating) {
        ratingSum += rating;
        ratingCount++;
    }

    private void removeRating(int rating) {
        if (ratingCount == 0) {
            throw new IllegalStateException("No ratings to remove");
        }
        ratingSum -= rating;
        ratingCount--;
    }
}
