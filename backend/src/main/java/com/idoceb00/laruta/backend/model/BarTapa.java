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

    @Column
    private Integer rating;

    public BarTapa(Bar bar, Tapa tapa, Integer rating) {
        this.bar = bar;
        this.tapa = tapa;
        this.rating = rating;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }
}
