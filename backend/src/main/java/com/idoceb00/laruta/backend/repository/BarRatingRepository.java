package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.BarRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarRatingRepository extends JpaRepository<BarRating, Long> {
    Optional<BarRating> findByBarIdAndUserId(Long barId, Long userId);
}
