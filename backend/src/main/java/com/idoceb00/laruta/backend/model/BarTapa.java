package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "bar_tapas", uniqueConstraints = {
    @UniqueConstraint(name = "UNIQUE_BAR_TAPA",columnNames = {"bar_id", "tapa_id"})
})
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BarTapa extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bar_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bar bar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tapa tapa;

    @Embedded
    @Getter(AccessLevel.NONE)
    private RatingStats ratingStats = new RatingStats();

    // Hibernate increments it on every update and rejects the save if another transaction changed the row first.
    // Managed by hibernate, never set it manually.
    @Version
    private Long version;

    public BarTapa(Bar bar, Tapa tapa) {
        this.bar = bar;
        this.tapa = tapa;
    }

    public Double getAverageRating() {
        return ratingStats.getAverageRating();
    }

    public long getRatingCount() {
        return ratingStats.getRatingCount();
    }

    public void replaceRating(Integer oldRating, Integer newRating) {
        if (newRating != null) {
            validateRating(newRating);
        }
        ratingStats.replaceRating(oldRating, newRating);
    }

    private void validateRating(int rating) {
        if (rating < 0 || rating > 5){
            throw new IllegalArgumentException("Rating must be between 0 and 5, got: " + rating);
        }
    }
}
