package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_COMMUNITY_USER", columnNames = {"community_id", "user_id"})
})
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityRole role;

    public Membership(Community community, User user, CommunityRole role) {
        this.community = community;
        this.user = user;
        this.role = role;
    }

    // Not exposed as an endpoint: only the leave rules promote the oldest member
    public void promoteToAdmin() {
        this.role = CommunityRole.ADMIN;
    }
}
