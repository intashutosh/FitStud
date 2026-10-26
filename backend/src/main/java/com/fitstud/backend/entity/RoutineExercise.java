package com.fitstud.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "routine_exercises",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_routine_exercises_position",
                        columnNames = {"routine_id", "position"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_routine_exercises_exercise_id",
                        columnList = "exercise_id"
                )
        }
)
public class RoutineExercise {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "routine_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_routine_exercises_routine"
            )
    )
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exercise_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_routine_exercises_exercise"
            )
    )
    private Exercise exercise;

    @Column(nullable = false)
    private int position;

    @Column(length = 300)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RoutineExercise() {
        // Required by JPA
    }

    public static RoutineExercise create(
            Routine routine,
            Exercise exercise,
            int position,
            String notes
    ) {
        if (routine == null) {
            throw new IllegalArgumentException(
                    "Routine cannot be null"
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

        RoutineExercise routineExercise = new RoutineExercise();
        routineExercise.routine = routine;
        routineExercise.exercise = exercise;
        routineExercise.setPosition(position);
        routineExercise.setNotes(notes);

        return routineExercise;
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

    public Routine getRoutine() {
        return routine;
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