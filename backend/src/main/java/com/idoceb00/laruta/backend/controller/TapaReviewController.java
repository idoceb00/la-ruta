package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.TapaReviewRequest;
import com.idoceb00.laruta.backend.dto.TapaReviewResponse;
import com.idoceb00.laruta.backend.service.TapaReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bars/{barId}/tapas/{tapaId}/review")
@RequiredArgsConstructor
public class TapaReviewController {

    private final TapaReviewService tapaReviewService;

    @Retryable(includes = OptimisticLockingFailureException.class, maxRetries = 3, delay = 50)
    @PutMapping
    public ResponseEntity<TapaReviewResponse> saveReview(@PathVariable Long barId, @PathVariable Long tapaId, @Valid @RequestBody TapaReviewRequest request) {
        return ResponseEntity.ok(tapaReviewService.saveReview(barId, tapaId, request));
    }

    @Retryable(includes = OptimisticLockingFailureException.class, maxRetries = 3, delay = 50)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long barId, @PathVariable Long tapaId) {
        tapaReviewService.deleteReview(barId, tapaId);
    }
}
