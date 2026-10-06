package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.BarReviewRequest;
import com.idoceb00.laruta.backend.dto.BarReviewResponse;
import com.idoceb00.laruta.backend.service.BarReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bars/{barId}/review")
@RequiredArgsConstructor
public class BarReviewController {

    private final BarReviewService barReviewService;


    // ResponseEntity is used because, depending on what happens in the method (create or update), one status or another
    // must be returned
    // Retries on concurrent updates (@Version in Bar); each attempt runs a new transaction.
    @Retryable(includes = OptimisticLockingFailureException.class, maxRetries = 3, delay = 50)
    @PutMapping
    public ResponseEntity<BarReviewResponse> saveReview(@PathVariable Long barId, @Valid @RequestBody BarReviewRequest barReviewRequest){
        return ResponseEntity.ok(barReviewService.saveReview(barId, barReviewRequest));
    }


    @Retryable(includes = OptimisticLockingFailureException.class, maxRetries = 3, delay = 50)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBarRating(@PathVariable Long barId) {
        barReviewService.deleteReview(barId);
    }

}
