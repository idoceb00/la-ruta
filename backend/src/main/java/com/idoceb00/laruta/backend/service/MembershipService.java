package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.exception.NotCommunityMemberException;
import com.idoceb00.laruta.backend.model.Membership;
import com.idoceb00.laruta.backend.repository.MembershipRepository;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Single place for the membership check: every community-scoped service calls it
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Membership requireMembership(Long communityId) {
        Long userId = currentUserProvider.getCurrentUserId();

        return membershipRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new NotCommunityMemberException(
                        "User " + userId + " is not a member of community " + communityId));
    }
}
