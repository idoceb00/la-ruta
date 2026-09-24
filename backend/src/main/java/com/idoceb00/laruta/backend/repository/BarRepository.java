package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Bar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarRepository extends JpaRepository<Bar, Long> {
    List<Bar> findByNameContainingIgnoreCase(String name);
    List<Bar> findByCity(String city);
    List<Bar> findByZone(String zone);
}
