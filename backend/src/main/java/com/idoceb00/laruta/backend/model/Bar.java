package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "bars")
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // JPA automatically converts them into columns without the need to add the tag to each one
    private String city;

    private String address;

    private String zone;

    @Column(length = 1000)
    private String notes;

    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    public Bar(String name, String city, String address, String zone, String notes) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.zone = zone;
        this.notes = notes;
    }
}
