package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByCommunityIdAndUserId(Long communityId, Long userId);
    List<Membership> findByUserId(Long userId);
    long countByCommunityId(Long communityId);
    // Oldest member first: the leave rules promote it when the admin leaves
    Optional<Membership> findFirstByCommunityIdAndUserIdNotOrderByIdAsc(Long communityId, Long userId);
}
