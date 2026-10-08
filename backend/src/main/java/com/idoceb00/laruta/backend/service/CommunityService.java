package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.CommunityRequest;
import com.idoceb00.laruta.backend.dto.CommunityResponse;
import com.idoceb00.laruta.backend.dto.JoinCommunityRequest;
import com.idoceb00.laruta.backend.exception.CommunityNotFoundException;
import com.idoceb00.laruta.backend.exception.MembershipAlreadyExistsException;
import com.idoceb00.laruta.backend.exception.NotCommunityMemberException;
import com.idoceb00.laruta.backend.exception.UserNotFoundException;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.CommunityRole;
import com.idoceb00.laruta.backend.model.Membership;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarReviewRepository;
import com.idoceb00.laruta.backend.repository.CommunityRepository;
import com.idoceb00.laruta.backend.repository.MembershipRepository;
import com.idoceb00.laruta.backend.repository.TapaReviewRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final BarReviewRepository barReviewRepository;
    private final TapaReviewRepository tapaReviewRepository;
    private final MembershipService membershipService;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public CommunityResponse createCommunity(CommunityRequest request) {
        User user = findUserOrThrow(currentUserProvider.getCurrentUserId());
        Community community = communityRepository.save(
                new Community(request.name(), generateUniqueInviteCode()));
        membershipRepository.save(new Membership(community, user, CommunityRole.ADMIN));

        return CommunityResponse.from(community, CommunityRole.ADMIN, 1);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> findMyCommunities() {
        return membershipRepository.findByUserId(currentUserProvider.getCurrentUserId())
                .stream()
                .map(membership -> CommunityResponse.from(
                        membership.getCommunity(),
                        membership.getRole(),
                        membershipRepository.countByCommunityId(membership.getCommunity().getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommunityResponse findById(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException(
                        "Community not found with id: " + communityId));
        Membership membership = membershipService.requireMembership(communityId);

        return CommunityResponse.from(
                community,
                membership.getRole(),
                membershipRepository.countByCommunityId(communityId));
    }

    @Transactional
    public CommunityResponse join(JoinCommunityRequest request) {
        // Codes are typed by hand on a phone: case and surrounding spaces are ignored
        String inviteCode = normalizeInviteCode(request.inviteCode());
        Community community = communityRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CommunityNotFoundException(
                        "Community not found with invite code: " + inviteCode));
        User user = findUserOrThrow(currentUserProvider.getCurrentUserId());

        if (membershipRepository.existsByCommunityIdAndUserId(community.getId(), user.getId())) {
            throw new MembershipAlreadyExistsException(
                    "User " + user.getId() + " is already a member of community " + community.getId());
        }
        membershipRepository.save(new Membership(community, user, CommunityRole.MEMBER));

        return CommunityResponse.from(
                community,
                CommunityRole.MEMBER,
                membershipRepository.countByCommunityId(community.getId()));
    }

    @Transactional
    public void leaveCommunity(Long communityId) {
        findCommunityOrThrow(communityId);
        leave(membershipService.requireMembership(communityId));
    }

    // Leave rules shared by the leave endpoint and account deletion.
    // Driven by the membership, never by the current user.
    @Transactional
    void leave(Membership membership) {
        Community community = membership.getCommunity();
        Long userId = membership.getUser().getId();

        // 1. The leaver's bar reviews go away and the bars lose their rating
        for (var review : barReviewRepository.findByUserIdAndBarCommunityId(userId, community.getId())) {
            review.getBar().replaceRating(review.getRating(), null);
            barReviewRepository.delete(review);
        }

        // 2. Same for tapa reviews, through their BarTapa
        for (var review : tapaReviewRepository.findByUserIdAndBarTapaBarCommunityId(userId, community.getId())) {
            review.getBarTapa().replaceRating(review.getRating(), null);
            tapaReviewRepository.delete(review);
        }

        // 3. The membership itself
        boolean wasAdmin = membership.getRole() == CommunityRole.ADMIN;
        membershipRepository.delete(membership);

        // 4. Last one out turns off the lights; otherwise an admin hand-off keeps the community alive
        if (membershipRepository.countByCommunityId(community.getId()) == 0) {
            communityRepository.delete(community);
        } else if (wasAdmin) {
            membershipRepository.findFirstByCommunityIdAndUserIdNotOrderByIdAsc(community.getId(), userId)
                    .ifPresent(Membership::promoteToAdmin);
        }
    }

    private String generateUniqueInviteCode() {
        String code = inviteCodeGenerator.generate();
        while (communityRepository.existsByInviteCode(code)) {
            code = inviteCodeGenerator.generate();
        }
        return code;
    }

    private String normalizeInviteCode(String inviteCode) {
        return inviteCode.trim().toUpperCase(Locale.ROOT);
    }

    private Community findCommunityOrThrow(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException(
                        "Community not found with id: " + communityId));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}
