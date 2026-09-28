package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.Tapa;

public record TapaResponse(
        Long id,
        String name
) {
    public static TapaResponse from(Tapa tapa) {
        return new TapaResponse(tapa.getId(), tapa.getName());
    }
}
