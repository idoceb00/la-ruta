package com.idoceb00.laruta.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarTapaTest {

    private BarTapa barTapa;

    @BeforeEach
    void setUp() {
        barTapa = new BarTapa(new Bar(
                new Community("Los del barrio", "ABCD2345"),
                "Anaikal",
                "León",
                "Calle Jesús Rubio",
                "Colegio San Claudio"
        ), new Tapa("alitas"));
    }

    @Test
    void newBarTapa_hasNoRatings() {
        assertThat(barTapa.getRatingCount()).isZero();
        assertThat(barTapa.getAverageRating()).isNull();
    }

    @Test
    void replaceRating_whenFirstRating_setsCountAndAverage() {
        barTapa.replaceRating(null, 3);

        assertThat(barTapa.getRatingCount()).isEqualTo(1);
        assertThat(barTapa.getAverageRating()).isEqualTo(3.0);
    }

    @Test
    void replaceRating_whenSeveralRatings_roundsAverageToOneDecimal() {
        barTapa.replaceRating(null, 4);
        barTapa.replaceRating(null, 3);
        barTapa.replaceRating(null, 4);

        assertThat(barTapa.getRatingCount()).isEqualTo(3);
        assertThat(barTapa.getAverageRating()).isEqualTo(3.7);
    }

    @Test
    void replaceRating_whenChanged_updatesAverageKeepingCount() {
        barTapa.replaceRating(null, 3);

        barTapa.replaceRating(3, 1);

        assertThat(barTapa.getRatingCount()).isEqualTo(1);
        assertThat(barTapa.getAverageRating()).isEqualTo(1.0);
    }

    @Test
    void replaceRating_whenSameValue_keepsAverage() {
        barTapa.replaceRating(null, 3);

        barTapa.replaceRating(3, 3);

        assertThat(barTapa.getRatingCount()).isEqualTo(1);
        assertThat(barTapa.getAverageRating()).isEqualTo(3.0);
    }

    @Test
    void replaceRating_whenRemovedWithSeveralRatings_recalculatesAverage() {
        barTapa.replaceRating(null, 5);
        barTapa.replaceRating(null, 2);
        barTapa.replaceRating(null, 3);

        barTapa.replaceRating(3, null);

        assertThat(barTapa.getRatingCount()).isEqualTo(2);
        assertThat(barTapa.getAverageRating()).isEqualTo(3.5);
    }

    @Test
    void replaceRating_whenLastRatingRemoved_resetsToNoRatings() {
        barTapa.replaceRating(null, 3);

        barTapa.replaceRating(3, null);

        assertThat(barTapa.getRatingCount()).isZero();
        assertThat(barTapa.getAverageRating()).isNull();
    }

    @Test
    void replaceRating_whenBothNull_keepsAggregates() {
        barTapa.replaceRating(null, 3);

        barTapa.replaceRating(null, null);

        assertThat(barTapa.getRatingCount()).isEqualTo(1);
        assertThat(barTapa.getAverageRating()).isEqualTo(3.0);
    }

    @Test
    void replaceRating_whenAddedOutOfRange_throwsAndKeepsAggregates() {
        assertThatThrownBy(() -> barTapa.replaceRating(null, 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> barTapa.replaceRating(null, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(barTapa.getRatingCount()).isZero();
        assertThat(barTapa.getAverageRating()).isNull();
    }

    @Test
    void replaceRating_whenChangedOutOfRange_throwsAndKeepsAggregates() {
        barTapa.replaceRating(null, 5);

        assertThatThrownBy(() -> barTapa.replaceRating(5, 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> barTapa.replaceRating(5, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(barTapa.getRatingCount()).isEqualTo(1);
        assertThat(barTapa.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void replaceRating_whenRemovingWithNoRatings_throwsException() {
        assertThatThrownBy(() -> barTapa.replaceRating(1, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
