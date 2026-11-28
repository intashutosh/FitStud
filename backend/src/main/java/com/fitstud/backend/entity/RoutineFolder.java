
package com.fitstud.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routine_folders")
public class RoutineFolder {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_routine_folders_user")
    )
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoutineFolder() {
    }

    public static RoutineFolder create(
            User user,
            String name,
            int position
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Folder owner cannot be null"
            );
        }

        RoutineFolder folder = new RoutineFolder();
        folder.user = user;
        folder.setName(name);
        folder.setPosition(position);

        return folder;
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
                    "Folder name cannot be blank"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Folder name cannot exceed 100 characters"
            );
        }

        this.name = normalizedName;
    }

    public void setPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException(
                    "Folder position cannot be negative"
            );
        }

        this.position = position;
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

    public int getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
