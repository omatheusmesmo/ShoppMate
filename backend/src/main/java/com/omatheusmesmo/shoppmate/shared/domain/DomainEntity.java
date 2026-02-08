package com.omatheusmesmo.shoppmate.shared.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class DomainEntity implements AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean deleted = false;

    public void checkName() {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (name.isBlank()) {
            throw new IllegalArgumentException("Enter a valid name!");
        }
    }
}