package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "bar_ratings", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_BAR_RATING", columnNames = {"bar_id", "user_id"})
})
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BarRating extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bar_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bar bar;

    // TODO: ON DELETE CASCADE bypasses Java, so deleting a user would leave Bar rating aggregates out of sync.
    //  User deletion must update affected bars before deleting.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    public BarRating(Bar bar, User user, Integer rating) {
        this.bar = bar;
        this.user = user;
        this.rating = rating;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }
}
