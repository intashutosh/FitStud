package com.fitstud.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "owner_id",
            foreignKey = @ForeignKey(name = "fk_exercises_owner")
    )
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "muscle_group", length = 50)
    private String muscleGroup;

    @Column(length = 50)
    private String equipment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Exercise() {
        // Required by JPA
    }

    public boolean isGlobal() {
        return owner == null;
    }

    public static Exercise createGlobal(
            String name,
            String description,
            String muscleGroup,
            String equipment) {

        return create(null, name, description, muscleGroup, equipment);
    }

    public static Exercise createCustom(
            User owner,
            String name,
            String description,
            String muscleGroup,
            String equipment) {

        if (owner == null) {
            throw new IllegalArgumentException(
                    "A custom exercise must have an owner");
        }

        return create(owner, name, description, muscleGroup, equipment);
    }

    private static Exercise create(
            User owner,
            String name,
            String description,
            String muscleGroup,
            String equipment) {

        Exercise exercise = new Exercise();

        exercise.owner = owner;
        exercise.setName(name);
        exercise.setDescription(description);
        exercise.setMuscleGroup(muscleGroup);
        exercise.setEquipment(equipment);

        return exercise;
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

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public String getEquipment() {
        return equipment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Exercise name cannot be blank");
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Exercise name cannot exceed 100 characters");
        }

        this.name = normalizedName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}
