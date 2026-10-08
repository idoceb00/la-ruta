package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.TapaReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TapaReviewRepository extends JpaRepository<TapaReview, Long> {
    Optional<TapaReview> findByBarTapaIdAndUserId(Long barTapaId, Long userId);

}
