package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.Bar;

import java.time.Instant;

public record BarResponse(
        Long id,
        Long communityId,
        String name,
        String city,
        String address,
        String zone,
        Double averageRating,
        long ratingCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static BarResponse from(Bar bar) {
        return new BarResponse(
                bar.getId(),
                bar.getCommunity().getId(),
                bar.getName(),
                bar.getCity(),
                bar.getAddress(),
                bar.getZone(),
                bar.getAverageRating(),
                bar.getRatingCount(),
                bar.getCreatedAt(),
                bar.getUpdatedAt()
        );
    }
}
