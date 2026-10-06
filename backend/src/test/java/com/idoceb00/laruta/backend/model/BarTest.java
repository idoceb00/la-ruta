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
                "Colegio San Claudio"
        );
    }

    @Test
    void newBar_hasNoRatings() {
        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    @Test
    void replaceRating_whenFirstRating_setsCountAndAverage() {
        bar.replaceRating(null, 8);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void replaceRating_whenSeveralRatings_roundsAverageToOneDecimal() {
        bar.replaceRating(null, 8);
        bar.replaceRating(null, 7);
        bar.replaceRating(null, 8);

        assertThat(bar.getRatingCount()).isEqualTo(3);
        assertThat(bar.getAverageRating()).isEqualTo(7.7);
    }

    @Test
    void replaceRating_whenChanged_updatesAverageKeepingCount() {
        bar.replaceRating(null, 8);

        bar.replaceRating(8, 6);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(6.0);
    }

    @Test
    void replaceRating_whenSameValue_keepsAverage() {
        bar.replaceRating(null, 8);

        bar.replaceRating(8, 8);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void replaceRating_whenRemovedWithSeveralRatings_recalculatesAverage() {
        bar.replaceRating(null, 10);
        bar.replaceRating(null, 5);
        bar.replaceRating(null, 6);

        bar.replaceRating(6, null);

        assertThat(bar.getRatingCount()).isEqualTo(2);
        assertThat(bar.getAverageRating()).isEqualTo(7.5);
    }

    @Test
    void replaceRating_whenLastRatingRemoved_resetsToNoRatings() {
        bar.replaceRating(null, 8);

        bar.replaceRating(8, null);

        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    // Notes-only edit: neither the old nor the new review has a rating
    @Test
    void replaceRating_whenBothNull_keepsAggregates() {
        bar.replaceRating(null, 8);

        bar.replaceRating(null, null);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void replaceRating_whenAddedOutOfRange_throwsAndKeepsAggregates() {
        assertThatThrownBy(() -> bar.replaceRating(null, 11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bar.replaceRating(null, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bar.getRatingCount()).isZero();
        assertThat(bar.getAverageRating()).isNull();
    }

    @Test
    void replaceRating_whenChangedOutOfRange_throwsAndKeepsAggregates() {
        bar.replaceRating(null, 5);

        assertThatThrownBy(() -> bar.replaceRating(5, 11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bar.replaceRating(5, -1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bar.getRatingCount()).isEqualTo(1);
        assertThat(bar.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void replaceRating_whenRemovingWithNoRatings_throwsException() {
        assertThatThrownBy(() -> bar.replaceRating(1, null))
                .isInstanceOf(IllegalStateException.class);
    }
}