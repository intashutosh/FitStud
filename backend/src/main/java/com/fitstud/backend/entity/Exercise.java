
package com.fitstud.backend.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fitstud.backend.enums.Equipment;
import com.fitstud.backend.enums.ExerciseCategory;
import com.fitstud.backend.enums.MechanicType;
import com.fitstud.backend.enums.MuscleGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "exercises", indexes = {
        @Index(name = "idx_exercise_primary_muscle",
                columnList = "primary_muscle"),
        @Index(name = "idx_exercise_custom_user",
                columnList = "is_custom, created_by_user_id")
})
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    @Size(max = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_muscle", nullable = false)
    private MuscleGroup primaryMuscle;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "exercise_secondary_muscles",
            joinColumns = @JoinColumn(name = "exercise_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_group", nullable = false)
    private Set<MuscleGroup> secondaryMuscles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_category", nullable = false)
    private ExerciseCategory category;

    @Column(name = "is_custom", nullable = false)
    private boolean custom = false;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Size(max = 500)
    private String instructions;

    @Column(name = "media_url", length = 2048)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "mechanic_type", nullable = false)
    private MechanicType mechanicType;

    @Min(1)
    @Max(3)
    @Column(name = "priority_tier", nullable = false)
    private int priorityTier = 2;

    @Positive
    @Column(name = "est_seconds_per_set", nullable = false)
    private int estimatedSecondsPerSet = 45;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exercise_alternatives",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "alternative_exercise_id")
    )
    @JsonIgnoreProperties("alternativeExercises")
    private Set<Exercise> alternativeExercises = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Exercise() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MuscleGroup getPrimaryMuscle() {
        return primaryMuscle;
    }

    public void setPrimaryMuscle(MuscleGroup primaryMuscle) {
        this.primaryMuscle = primaryMuscle;
    }

    public Set<MuscleGroup> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(Set<MuscleGroup> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public ExerciseCategory getCategory() {
        return category;
    }

    public void setCategory(ExerciseCategory category) {
        this.category = category;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public MechanicType getMechanicType() {
        return mechanicType;
    }

    public void setMechanicType(MechanicType mechanicType) {
        this.mechanicType = mechanicType;
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public void setPriorityTier(int priorityTier) {
        this.priorityTier = priorityTier;
    }

    public int getEstimatedSecondsPerSet() {
        return estimatedSecondsPerSet;
    }

    public void setEstimatedSecondsPerSet(int estimatedSecondsPerSet) {
        this.estimatedSecondsPerSet = estimatedSecondsPerSet;
    }

    public Set<Exercise> getAlternativeExercises() {
        return alternativeExercises;
    }

    public void setAlternativeExercises(Set<Exercise> alternativeExercises) {
        this.alternativeExercises = alternativeExercises;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
