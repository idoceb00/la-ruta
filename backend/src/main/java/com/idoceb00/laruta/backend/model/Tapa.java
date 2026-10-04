package com.idoceb00.laruta.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tapas")
@Getter
// Constructor required for reading rows via JPA
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tapa extends BaseEntity{

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Tapa(String name){
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }
}
