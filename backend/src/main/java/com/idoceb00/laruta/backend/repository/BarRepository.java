package com.idoceb00.laruta.backend.repository;

import com.idoceb00.laruta.backend.model.Bar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BarRepository extends JpaRepository<Bar, Long> {
    List<Bar> findByCommunityId(Long communityId);

    // The same physical bar can exist in several communities: search never crosses them
    @Query("""
        SELECT b FROM Bar b
        WHERE b.community.id = :communityId
          AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.city) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.zone) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    List<Bar> searchInCommunity(@Param("communityId") Long communityId, @Param("query") String query);
}
