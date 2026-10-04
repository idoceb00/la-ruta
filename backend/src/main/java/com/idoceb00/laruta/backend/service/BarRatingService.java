package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarRatingRequest;
import com.idoceb00.laruta.backend.dto.BarRatingResponse;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.BarRatingNotFoundException;
import com.idoceb00.laruta.backend.exception.UserNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarRating;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRatingRepository;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BarRatingService {

    private final BarRatingRepository barRatingRepository;
    private final BarRepository barRepository;
    private final UserRepository userRepository;

    @Transactional
    public BarRatingResponse rateBar(Long barId, BarRatingRequest barRatingRequest){
        Bar bar = findBarOrThrow(barId);
        User user = findUserOrThrow(barRatingRequest.userId());
        int newRating = barRatingRequest.rating();

        BarRating barRating = barRatingRepository.findByBarIdAndUserId(barId, user.getId())
                .map(existing -> {
                    // Old value must be read before updating the rating
                    bar.changeRating(existing.getRating(), newRating);
                    existing.updateRating(newRating);
                   return existing;
                })
                .orElseGet(() ->{
                        bar.addRating(newRating);
                        return barRatingRepository.save(new BarRating(bar, user, newRating));
                });

        return BarRatingResponse.from(
                barRating,
                bar.getAverageRating(),
                bar.getRatingCount()
        );
    }

    @Transactional
    public void deleteRating(Long barId, Long userId) {
        BarRating barRating = barRatingRepository.findByBarIdAndUserId(barId, userId)
                .orElseThrow(() -> new BarRatingNotFoundException("Rating not found for bar " + barId + " and user " + userId));

        barRating.getBar().removeRating(barRating.getRating());
        barRatingRepository.delete(barRating);
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
