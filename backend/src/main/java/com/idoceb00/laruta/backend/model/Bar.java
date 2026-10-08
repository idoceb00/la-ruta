package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "bars")
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bar extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Community community;

    @Column(nullable = false)
    private String name;

    // JPA automatically converts them into columns without the need to add the tag to each one
    private String city;

    private String address;

    private String zone;

    @Embedded
    @Getter(AccessLevel.NONE)
    private RatingStats ratingStats = new RatingStats();

    // Hibernate increments it on every update and rejects the save if another transaction changed the row first.
    // Managed by hibernate, never set it manually.
    @Version
    private Long version;

    public Bar(Community community, String name, String city, String address, String zone) {
        this.community = community;
        this.name = name;
        this.city = city;
        this.address = address;
        this.zone = zone;
    }

    public void update(String name, String city, String address, String zone) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.zone = zone;
    }

    public Double getAverageRating() {
        return ratingStats.getAverageRating();
    }

    public long getRatingCount() {
        return ratingStats.getRatingCount();
    }

    // Single entry point for rating changes
    public void replaceRating(Integer oldRating, Integer newRating) {
        if (newRating != null) {
            validateRating(newRating);
        }
        ratingStats.replaceRating(oldRating, newRating);
    }

    private void validateRating(int rating) {
        if (rating < 0 || rating > 10){
            throw new IllegalArgumentException("Rating must be between 0 and 10, got: " + rating);
        }
    }
}
