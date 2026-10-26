package com.fitstud.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class Routine {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_routines_user")
    )
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Routine() {
        // Required by JPA
    }

    public static Routine create(
            User user,
            String name,
            String description
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Routine owner cannot be null"
            );
        }

        Routine routine = new Routine();
        routine.user = user;
        routine.setName(name);
        routine.setDescription(description);

        return routine;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Routine name cannot be blank"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Routine name cannot exceed 100 characters"
            );
        }

        this.name = normalizedName;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 500 characters"
            );
        }

        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}