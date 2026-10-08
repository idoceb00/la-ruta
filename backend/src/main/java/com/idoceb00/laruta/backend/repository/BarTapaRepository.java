package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.BarTapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BarTapaRepository extends JpaRepository<BarTapa, Long> {
    List<BarTapa> findByBarId(Long barId);
    List<BarTapa> findByTapaId(Long tapaId);
    Optional<BarTapa> findByBarIdAndTapaId(Long barId, Long tapaId);
    boolean existsByBarIdAndTapaId(Long barId, Long tapaId);
    // Bars serving a tapa, best rated first. Unrated ones go last; with the same average, more ratings first.
    @Query("""
        SELECT bt FROM BarTapa bt
        JOIN FETCH bt.bar
        JOIN FETCH bt.tapa
        WHERE bt.tapa.id = :tapaId
        ORDER BY bt.ratingStats.ratingSum * 1.0 / NULLIF(bt.ratingStats.ratingCount, 0) DESC NULLS LAST,
                 bt.ratingStats.ratingCount DESC
        """)
    List<BarTapa> findByTapaIdOrderByAverageRatingDesc(@Param("tapaId") Long tapaId);
}
