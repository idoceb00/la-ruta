package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "bar_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_BAR_REVIEW", columnNames = {"bar_id", "user_id"})
})
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BarReview extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bar_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bar bar;

    // Safety net only: account deletion removes reviews in Java first (leave rules),
    // so rating aggregates are updated before this cascade could apply
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(length = 1000)
    private String notes;

    private Integer rating;

    public BarReview(Bar bar, User user, String notes, Integer rating) {
        this.bar = bar;
        this.user = user;
        this.notes = notes;
        this.rating = rating;
    }

    public void update(String notes, Integer rating) {
        this.notes = notes;
        this.rating = rating;
    }
}
