package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "bars")
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bar extends BaseEntity {

    @Column(nullable = false)
    private String name;

    // JPA automatically converts them into columns without the need to add the tag to each one
    private String city;

    private String address;

    private String zone;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private long ratingSum = 0;

    @Column(nullable = false)
    private long ratingCount = 0;

    // Hibernate increments it on every update and rejects the save if another transaction changed the row first.
    // Managed by hibernate, never set it manually.
    @Version
    private Long version;

    public Bar(String name, String city, String address, String zone, String notes) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.zone = zone;
        this.notes = notes;
    }

    public void update(String name, String city, String address, String zone, String notes) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.zone = zone;
        this.notes = notes;
    }

    public Double getAverageRating() {
        if (ratingCount == 0){
            return null;
        }

        double average = (double) ratingSum / ratingCount;
        return Math.round(average * 10) / 10.0;
    }

    public void addRating(int rating) {
        validateRating(rating);
        ratingSum += rating;
        ratingCount++;
    }

    public void changeRating(int oldRating, int newRating) {
        validateRating(newRating);
        ratingSum += newRating - oldRating;
    }

    public void removeRating(int rating) {
        if (ratingCount == 0) {
            throw new IllegalStateException("Bar has no ratings to remove");
        }
        ratingSum -= rating;
        ratingCount--;
    }

    private void validateRating(int rating) {
        if (rating < 0 || rating > 10){
            throw new IllegalArgumentException("Rating must be between 0 and 10, got: " + rating);
        }
    }
}
