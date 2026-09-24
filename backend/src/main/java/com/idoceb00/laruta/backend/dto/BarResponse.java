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
        Instant createdAt
) {
    public static BarResponse from(Bar bar) {
        return new BarResponse(
                bar.getId(),
                bar.getName(),
                bar.getCity(),
                bar.getAddress(),
                bar.getZone(),
                bar.getNotes(),
                bar.getCreatedAt()
        );
    }
}
