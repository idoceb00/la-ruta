package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarDetailResponse;
import com.idoceb00.laruta.backend.dto.BarResponse;
import com.idoceb00.laruta.backend.dto.BarRequest;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.CommunityNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarReview;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.TapaReview;
import com.idoceb00.laruta.backend.repository.*;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarService {

    private final BarRepository barRepository;
    private final CommunityRepository communityRepository;
    private final MembershipService membershipService;
    private final BarReviewRepository barReviewRepository;
    private final BarTapaRepository barTapaRepository;
    private final TapaReviewRepository tapaReviewRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public BarResponse createBar(Long communityId, BarRequest request) {
        Community community = findCommunityOrThrow(communityId);
        membershipService.requireMembership(communityId);

        Bar bar = barRepository.save(new Bar(
                community,
                request.name(),
                request.city(),
                request.address(),
                request.zone()
        ));

        return BarResponse.from(bar);
    }

    @Transactional(readOnly = true)
    public List<BarResponse> getBars(Long communityId, String query) {
        findCommunityOrThrow(communityId);
        membershipService.requireMembership(communityId);

        List<Bar> bars = (query == null || query.isBlank())
                ? barRepository.findByCommunityId(communityId)
                : barRepository.searchInCommunity(communityId, query);

        return toResponses(bars);
    }

    @Transactional(readOnly = true)
    public BarDetailResponse findById(Long id) {
        Bar bar = findBarOrThrow(id);
        membershipService.requireMembership(bar.getCommunity().getId());
        Long userId = currentUserProvider.getCurrentUserId();

        BarReview myReview = barReviewRepository.findByBarIdAndUserId(id, userId).orElse(null);

        // Indexed by bar_tapa id so each tapa finds its review without extra queries
        Map<Long, TapaReview> myTapaReviews = tapaReviewRepository.findByUserIdAndBarTapaBarId(userId, id)
                .stream()
                .collect(Collectors.toMap(review -> review.getBarTapa().getId(), Function.identity()));

        List<BarDetailResponse.TapaDetail> tapas = barTapaRepository.findByBarIdWithTapa(id)
                .stream()
                .map(barTapa -> BarDetailResponse.TapaDetail.from(barTapa, myTapaReviews.get(barTapa.getId())))
                .toList();

        return BarDetailResponse.from(bar, myReview, tapas);
    }

    @Transactional
    public BarResponse updateBar(Long id, BarRequest request) {
        Bar bar = findBarOrThrow(id);
        membershipService.requireMembership(bar.getCommunity().getId());

        bar.update(request.name(), request.city(), request.address(), request.zone());

        return BarResponse.from(bar);
    }

    @Transactional
    public void deleteById(Long id) {
        Bar bar = findBarOrThrow(id);
        membershipService.requireMembership(bar.getCommunity().getId());

        barRepository.delete(bar);
    }

    private Bar findBarOrThrow(Long id) {
        return barRepository.findById(id).orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + id));
    }

    private Community findCommunityOrThrow(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found with id: " + communityId));
    }

    private List<BarResponse> toResponses(List<Bar> bars) {
        return bars.stream().map(BarResponse::from).toList();
    }
}
