package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.Bar;

import java.time.Instant;

public record BarResponse(
        Long id,
        String name,
        String city,
        String address,
        String zone,
        String notes,
        Double avgRating,
        long ratingCount,
        Instant createdAt
) {
    public static BarResponse from(Bar bar, Double avgRating, long ratingCount) {
        return new BarResponse(
                bar.getId(),
                bar.getName(),
                bar.getCity(),
                bar.getAddress(),
                bar.getZone(),
                bar.getNotes(),
                avgRating,
                ratingCount,
                bar.getCreatedAt()
        );
    }
}
