package com.idoceb00.laruta.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the rating aggregates in {@link Bar}.
 *
 * <p>Pure JUnit 5 tests: no Spring context or database, so they run in milliseconds.
 *
 * <p>Assertions use AssertJ ({@code assertThat}) instead of JUnit's {@code assertEquals}/{@code assertTrue}:
 * failure messages show the actual value ("expected: 1 but was: 2"), the IDE suggests
 * only the checks that fit each type, and exceptions are tested with {@code assertThatThrownBy}.
 * AssertJ comes with the Spring Boot test starters, so no extra dependency is needed.
 */
class BarTest {

    private Bar bar;

    @BeforeEach
    void setUp() {
        bar = new Bar(
                "Anaikal",
                "León",
                "Calle Jesús Rubio",
                "Colegio San Claudio",
                "El arroz picante está realmente bueno."
        );
    }

    @Test
    void newBar_hasNoRatings() {
        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    @Test
    void addRating_whenFirstRating_setsCountAndAverage() {
        bar.addRating(8);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void addRating_whenSeveralRatings_roundsAverageToOneDecimal() {
        bar.addRating(8);
        bar.addRating(7);
        bar.addRating(8);

        assertThat(bar.getRatingCount()).isEqualTo(3);
        assertThat(bar.getAverageRating()).isEqualTo(7.7);
    }

    @Test
    void changeRating_updatesAverageKeepingCount() {
        bar.addRating(8);

        bar.changeRating(8, 6);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(6.0);
    }

    @Test
    void changeRating_whenSameValue_keepsAverage() {
        bar.addRating(8);

        bar.changeRating(8, 8);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void removeRating_whenSeveralRatings_recalculatesAverage() {
        bar.addRating(10);
        bar.addRating(5);
        bar.addRating(6);

        bar.removeRating(6);

        assertThat(bar.getRatingCount()).isEqualTo(2);
        assertThat(bar.getAverageRating()).isEqualTo(7.5);
    }

    @Test
    void removeRating_whenLastRating_resetsToNoRatings() {
        bar.addRating(8);

        bar.removeRating(8);

        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    @Test
    void addRating_whenOutOfRange_throwsException() {
        assertThatThrownBy(() -> bar.addRating(11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bar.addRating(-1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    @Test
    void changeRating_whenOutOfRange_throwsException() {
        bar.addRating(5);

        assertThatThrownBy(() -> bar.changeRating(5, 11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bar.changeRating(5, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void removeRating_whenNoRatings_throwsException() {
        assertThatThrownBy(() -> bar.removeRating(1))
                .isInstanceOf(IllegalStateException.class);
    }
}