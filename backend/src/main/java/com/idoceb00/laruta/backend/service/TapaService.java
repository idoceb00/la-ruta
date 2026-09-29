package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.TapaResponse;
import com.idoceb00.laruta.backend.exception.TapaNotFoundException;
import com.idoceb00.laruta.backend.model.Tapa;
import com.idoceb00.laruta.backend.repository.TapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TapaService {

    private final TapaRepository tapaRepository;

    // Used by other services: returns the entity not a DTO
    @Transactional
    public Tapa findOrCreate(String name) {
        String normalizedName = normalizeName(name);
        return tapaRepository.findByNameIgnoreCase(normalizedName).
                orElseGet(() -> tapaRepository.save(new Tapa(normalizedName)));
    }

    public Tapa getById(Long id) {
        return tapaRepository.findById(id).
                orElseThrow(() -> new TapaNotFoundException("Tapa not found with id: " + id));
    }

    // Autocomplete: nothing until the user types something
    @Transactional(readOnly = true)
    public List<TapaResponse> searchTapas(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return tapaRepository.findTop10ByNameContainingIgnoreCase(normalizeName(query))
                .stream().map(TapaResponse::from).toList();
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}
