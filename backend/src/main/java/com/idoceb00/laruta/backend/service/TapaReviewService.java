package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.TapaReviewRequest;
import com.idoceb00.laruta.backend.dto.TapaReviewResponse;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.BarTapaNotFoundException;
import com.idoceb00.laruta.backend.exception.TapaReviewNotFoundException;
import com.idoceb00.laruta.backend.exception.UserNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.TapaReview;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.BarTapaRepository;
import com.idoceb00.laruta.backend.repository.TapaReviewRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TapaReviewService {

    private final TapaReviewRepository tapaReviewRepository;
    private final BarTapaRepository barTapaRepository;
    private final BarRepository barRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final MembershipService membershipService;

    @Transactional
    public TapaReviewResponse saveReview(Long barId, Long tapaId, TapaReviewRequest tapaReviewRequest) {
        Bar bar = findBarOrThrow(barId);
        User user = findUserOrThrow(currentUserProvider.getCurrentUserId());
        membershipService.requireMembership(bar.getCommunity().getId());
        BarTapa barTapa = findBarTapaOrThrow(barId, tapaId);
        String notes = tapaReviewRequest.notes();
        Integer newRating = tapaReviewRequest.rating();
        boolean fav = tapaReviewRequest.fav();

        TapaReview tapaReview = tapaReviewRepository.findByBarTapaIdAndUserId(barTapa.getId(), user.getId())
                .map(existing -> {
                    Integer oldRating = existing.getRating();
                    barTapa.replaceRating(oldRating, newRating);
                    existing.update(notes, newRating, fav);
                    return existing;
                })
                .orElseGet(() -> {
                    barTapa.replaceRating(null, newRating);
                    return tapaReviewRepository.save(new TapaReview(barTapa, user, notes, newRating, fav));
                });

        return TapaReviewResponse.from(
                tapaReview,
                barTapa.getAverageRating(),
                barTapa.getRatingCount()
        );
    }


    @Transactional
    public void deleteReview(Long barId, Long tapaId) {
        Bar bar = findBarOrThrow(barId);
        membershipService.requireMembership(bar.getCommunity().getId());
        BarTapa barTapa = findBarTapaOrThrow(barId, tapaId);
        Long userId = currentUserProvider.getCurrentUserId();
        TapaReview tapaReview = tapaReviewRepository.findByBarTapaIdAndUserId(barTapa.getId(), userId)
                .orElseThrow(() -> new TapaReviewNotFoundException(
                        "Review not found for tapa " + tapaId + " in bar " + barId + " and user " + userId));

        tapaReview.getBarTapa().replaceRating(tapaReview.getRating(), null);
        tapaReviewRepository.delete(tapaReview);
    }

    private Bar findBarOrThrow(Long barId) {
        return barRepository.findById(barId)
                .orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + barId));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private BarTapa findBarTapaOrThrow(Long barId, Long tapaId) {
        return barTapaRepository.findByBarIdAndTapaId(barId, tapaId)
                .orElseThrow(() -> new BarTapaNotFoundException("Tapa with id: " + tapaId + " not found in bar with id: " + barId));
    }
}
