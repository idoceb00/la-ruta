package com.idoceb00.laruta.backend.model;

import com.idoceb00.laruta.backend.repository.BarRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BaseEntityAuditingTest {

    @Autowired
    private BarRepository barRepository;

    private Long barId;

    @AfterEach
    void cleanUp() {
        if (barId != null) {
            barRepository.deleteById(barId);
        }
    }

    @Test
    void shouldSetAuditTimestampsOnCreate() {
        barId = barRepository.save(newBar()).getId();

        Bar found = barRepository.findById(barId).orElseThrow();

        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldChangeOnlyUpdatedAtOnModification() {
        barId = barRepository.save(newBar()).getId();

        // Read back from the DB so both timestamps have the DB precision (microseconds)
        Bar original = barRepository.findById(barId).orElseThrow();
        Instant originalCreatedAt = original.getCreatedAt();
        Instant originalUpdatedAt = original.getUpdatedAt();

        // A real change is required, otherwise Hibernate skips the UPDATE
        original.update("Updated Name", "León", "Calle Ancha 1", "Centro");
        barRepository.save(original);

        Bar modified = barRepository.findById(barId).orElseThrow();

        assertThat(modified.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(modified.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    private Bar newBar() {
        return new Bar("Test Bar", "León", "Calle Ancha 1", "Centro");
    }
}