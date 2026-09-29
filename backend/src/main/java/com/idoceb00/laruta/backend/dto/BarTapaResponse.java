package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.BarTapa;

public record BarTapaResponse(
        Long barId,
        String barName,
        Long tapaId,
        String tapaName,
        Integer rating
) {
    public static BarTapaResponse from(BarTapa barTapa) {
        return new BarTapaResponse(
                barTapa.getBar().getId(),
                barTapa.getBar().getName(),
                barTapa.getTapa().getId(),
                barTapa.getTapa().getName(),
                barTapa.getRating()
        );
    }
}
