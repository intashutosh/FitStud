package com.fitstud.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "workout_exercises",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_workout_exercises_position",
                        columnNames = {"workout_id", "position"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_workout_exercises_exercise",
                        columnList = "exercise_id"
                )
        }
)
public class WorkoutExercise {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workout_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_workout_exercises_workout"
            )
    )
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exercise_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_workout_exercises_exercise"
            )
    )
    private Exercise exercise;

    @Column(nullable = false)
    private int position;

    @Column(length = 300)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkoutExercise() {
    }

    public static WorkoutExercise create(
            Workout workout,
            Exercise exercise,
            int position,
            String notes
    ) {
        if (workout == null) {
            throw new IllegalArgumentException(
                    "Workout cannot be null"
            );
        }

        if (exercise == null) {
            throw new IllegalArgumentException(
                    "Exercise cannot be null"
            );
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position cannot be negative"
            );
        }

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.workout = workout;
        workoutExercise.exercise = exercise;
        workoutExercise.setPosition(position);
        workoutExercise.setNotes(notes);

        return workoutExercise;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void setPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position cannot be negative"
            );
        }

        this.position = position;
    }

    public void setNotes(String notes) {
        if (notes != null && notes.length() > 300) {
            throw new IllegalArgumentException(
                    "Notes cannot exceed 300 characters"
            );
        }

        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getPosition() {
        return position;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}