package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.dto.BarRatingStats;
import com.idoceb00.laruta.backend.model.BarRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BarRatingRepository extends JpaRepository<BarRating, Long> {
    Optional<BarRating> findByBarIdAndUserId(Long barId, Long userId);

    // A single database query for a basic calculation. If business logic were involved,
    // the calculation would be carried out in the domain
    @Query("SELECT AVG(r.rating) FROM BarRating r WHERE r.bar.id = :barId")
    Optional<Double> findAverageRatingByBarId(@Param("barId") Long barId);

    long countByBarId(Long barId);

    @Query("""
        SELECT new com.idoceb00.laruta.backend.repository.projection.BarRatingStats(
            r.bar.id, AVG(r.rating), COUNT(r)
        )
        FROM BarRating r
        WHERE r.bar.id IN :barIds
        GROUP BY r.bar.id
        """)
    List<BarRatingStats> findStatsByBarId(@Param("barIds") Collection<Long> barIds);
}
