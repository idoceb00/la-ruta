package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tapa_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_TAPA_REVIEW", columnNames = {"bar_tapa_id", "user_id"})
})
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TapaReview extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bar_tapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BarTapa barTapa;

    // TODO: ON DELETE CASCADE bypasses Java, so deleting a user would leave BarTapa rating aggregates out of sync.
    //  User deletion must update affected bar tapas before deleting.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(length = 1000)
    private String notes;

    private Integer rating;

    private boolean fav = false;

    public TapaReview(BarTapa barTapa, User user, String notes, Integer rating, boolean fav) {
        this.barTapa = barTapa;
        this.user = user;
        this.notes = notes;
        this.rating = rating;
        this.fav = fav;
    }

    public void update(String notes, Integer rating, boolean fav){
        this.notes = notes;
        this.rating = rating;
        this.fav = fav;
    }
}
