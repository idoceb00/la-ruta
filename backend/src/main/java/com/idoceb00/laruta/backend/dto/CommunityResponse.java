package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.CommunityRole;

public record CommunityResponse(
        Long id,
        String name,
        String inviteCode,
        CommunityRole role,
        long memberCount
) {
    public static CommunityResponse from(Community community, CommunityRole role, long memberCount) {
        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getInviteCode(),
                role,
                memberCount
        );
    }
}
