package com.fitstud.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "workout_sets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_workout_sets_number",
                        columnNames = {"workout_exercise_id", "set_number"}
                )
        }
)
public class WorkoutSet {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workout_exercise_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_workout_sets_workout_exercise"
            )
    )
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "reps")
    private Integer reps;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkoutSet() {
    }

    public static WorkoutSet create(
            WorkoutExercise workoutExercise,
            int setNumber,
            BigDecimal weightKg,
            Integer reps
    ) {
        if (workoutExercise == null) {
            throw new IllegalArgumentException(
                    "Workout exercise cannot be null"
            );
        }

        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.workoutExercise = workoutExercise;
        workoutSet.setSetNumber(setNumber);
        workoutSet.setWeightKg(weightKg);
        workoutSet.setReps(reps);
        workoutSet.completed = false;
        workoutSet.completedAt = null;

        return workoutSet;
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

    public void setSetNumber(int setNumber) {
        if (setNumber < 1) {
            throw new IllegalArgumentException(
                    "Set number must be at least 1"
            );
        }

        this.setNumber = setNumber;
    }

    public void setWeightKg(BigDecimal weightKg) {
        if (weightKg != null
                && weightKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Weight cannot be negative"
            );
        }

        this.weightKg = weightKg;
    }

    public void setReps(Integer reps) {
        if (reps != null && reps < 1) {
            throw new IllegalArgumentException(
                    "Reps must be at least 1"
            );
        }

        this.reps = reps;
    }

    public void markCompleted() {
        if (weightKg == null || reps == null) {
            throw new IllegalStateException(
                    "Enter weight and reps before completing a set"
            );
        }

        this.completed = true;
        this.completedAt = Instant.now();
    }

    public void markIncomplete() {
        this.completed = false;
        this.completedAt = null;
    }

    public UUID getId() {
        return id;
    }

    public WorkoutExercise getWorkoutExercise() {
        return workoutExercise;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public Integer getReps() {
        return reps;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}