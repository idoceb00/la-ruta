package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.BarReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarReviewRepository extends JpaRepository<BarReview, Long> {
    Optional<BarReview> findByBarIdAndUserId(Long barId, Long userId);
}
