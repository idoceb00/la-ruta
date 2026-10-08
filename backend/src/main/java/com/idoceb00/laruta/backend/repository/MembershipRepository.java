package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByCommunityIdAndUserId(Long communityId, Long userId);

    // Community fetched in the same query: listing a user's communities must not lazy-load one per row
    @Query("""
        SELECT m FROM Membership m
        JOIN FETCH m.community
        WHERE m.user.id = :userId
        """)
    List<Membership> findByUserId(@Param("userId") Long userId);

    long countByCommunityId(Long communityId);

    // Excludes the leaving user on purpose: their own membership is usually the oldest one
    Optional<Membership> findFirstByCommunityIdAndUserIdNotOrderByIdAsc(Long communityId, Long userId);
}
