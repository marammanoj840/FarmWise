package com.farmwise.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseAuditEntity {

    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    protected RoleEntity() {
    }

    public RoleEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
