package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.BarRatingRequest;
import com.idoceb00.laruta.backend.dto.BarRatingResponse;
import com.idoceb00.laruta.backend.service.BarRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bars/{barId}/rating")
@RequiredArgsConstructor
public class BarRatingController {

    private final BarRatingService barRatingService;


    // ResponseEntity is used because, depending on what happens in the method (create or update),
    // one status or another must be returned
    @PutMapping
    public ResponseEntity<BarRatingResponse> rateBar(@PathVariable Long barId, @Valid @RequestBody BarRatingRequest barRatingRequest){
        return ResponseEntity.ok(barRatingService.rateBar(barId, barRatingRequest));
    }


    // TODO: userId as query param is temporary until auth is implemented
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBarRating(@PathVariable Long barId, @RequestParam Long userId) {
        barRatingService.deleteRating(barId, userId);
    }

}
