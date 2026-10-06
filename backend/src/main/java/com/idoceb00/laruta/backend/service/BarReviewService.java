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

    @Transactional
    public BarReviewResponse rateBar(Long barId, BarReviewRequest barReviewRequest){
        Bar bar = findBarOrThrow(barId);
        User user = findUserOrThrow(currentUserProvider.getCurrentUserId());
        int newRating = barReviewRequest.rating();

        BarReview barReview = barReviewRepository.findByBarIdAndUserId(barId, user.getId())
                .map(existing -> {
                    // Old value must be read before updating the rating
                    bar.changeRating(existing.getRating(), newRating);
                    existing.updateRating(newRating);
                   return existing;
                })
                .orElseGet(() ->{
                        bar.addRating(newRating);
                        return barReviewRepository.save(new BarReview(bar, user, newRating));
                });

        return BarReviewResponse.from(
                barReview,
                bar.getAverageRating(),
                bar.getRatingCount()
        );
    }

    @Transactional
    public void deleteRating(Long barId) {
        Long userId = currentUserProvider.getCurrentUserId();
        BarReview barReview = barReviewRepository.findByBarIdAndUserId(barId, userId)
                .orElseThrow(() -> new BarReviewNotFoundException("Review not found for bar " + barId + " and user " + userId));

        barReview.getBar().removeRating(barReview.getRating());
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
