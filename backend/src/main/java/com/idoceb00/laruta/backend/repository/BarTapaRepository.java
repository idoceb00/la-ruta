package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.BarTapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BarTapaRepository extends JpaRepository<BarTapa, Long> {
    List<BarTapa> findByBarId(Long barId);
    List<BarTapa> findByTapaIdOrderByRatingDesc(Long tapaId);
    Optional<BarTapa> findByBarIdAndTapaId(Long barId, Long tapaId);
    boolean existsByBarIdAndTapaId(Long barId, Long tapaId);
}
