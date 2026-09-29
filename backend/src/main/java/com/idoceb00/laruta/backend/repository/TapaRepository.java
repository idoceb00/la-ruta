package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Tapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TapaRepository extends JpaRepository<Tapa, Long> {
    Optional<Tapa> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Tapa> findTop10ByNameContainingIgnoreCase(String name);
}
