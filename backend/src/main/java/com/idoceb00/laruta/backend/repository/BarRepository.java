package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Bar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarRepository extends JpaRepository<Bar, Long> {
    // Short alias for the derived query: one DB call, readable name
    List<Bar> findByNameContainingIgnoreCaseOrCityIgnoreCaseOrZoneIgnoreCase(String name, String city, String zone);
    default List<Bar> search(String query) {
        return findByNameContainingIgnoreCaseOrCityIgnoreCaseOrZoneIgnoreCase(query, query, query);
    }
}
