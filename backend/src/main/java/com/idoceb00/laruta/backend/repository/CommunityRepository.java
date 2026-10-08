package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}
