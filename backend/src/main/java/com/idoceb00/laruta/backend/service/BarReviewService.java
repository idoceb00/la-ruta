package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarReviewRequest;
import com.idoceb00.laruta.backend.dto.BarReviewResponse;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.BarReviewNotFoundException;
import com.idoceb00.laruta.backend.exception.UserNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarReview;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarReviewRepository;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BarReviewService {

    private final BarReviewRepository barReviewRepository;
    private final BarRepository barRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final MembershipService membershipService;

    @Transactional
    public BarReviewResponse saveReview(Long barId, BarReviewRequest barReviewRequest){
        Bar bar = findBarOrThrow(barId);
        User user = findUserOrThrow(currentUserProvider.getCurrentUserId());
        membershipService.requireMembership(bar.getCommunity().getId());
        String notes = barReviewRequest.notes();
        Integer newRating = barReviewRequest.rating();

        BarReview barReview = barReviewRepository.findByBarIdAndUserId(barId, user.getId())
                .map(existing -> {
                    // Old value must be read before updating the review
                    Integer oldRating = existing.getRating();
                    bar.replaceRating(oldRating, newRating);
                    existing.update(notes, newRating);
                    return existing;
                })
                .orElseGet(() -> {
                    bar.replaceRating(null, newRating);
                    return barReviewRepository.save(new BarReview(bar, user, notes, newRating));
                });

        return BarReviewResponse.from(
                barReview,
                bar.getAverageRating(),
                bar.getRatingCount()
        );
    }

    @Transactional
    public void deleteReview(Long barId) {
        Bar bar = findBarOrThrow(barId);
        membershipService.requireMembership(bar.getCommunity().getId());
        Long userId = currentUserProvider.getCurrentUserId();
        BarReview barReview = barReviewRepository.findByBarIdAndUserId(barId, userId)
                .orElseThrow(() -> new BarReviewNotFoundException("Review not found for bar " + barId + " and user " + userId));

        barReview.getBar().replaceRating(barReview.getRating(), null);
        barReviewRepository.delete(barReview);
    }

    private Bar findBarOrThrow(Long barId) {
        return barRepository.findById(barId)
                .orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + barId));
    }

    private User findUserOrThrow(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}
