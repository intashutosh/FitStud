package com.fitstud.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitstud.backend.enums.SetType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "workout_sets", indexes = {
        @Index(name = "idx_ws_workout_exercise_order", columnList = "workout_exercise_id, set_order")
})
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_order", nullable = false)
    private int setOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "set_type", nullable = false, length = 20)
    private SetType setType = SetType.NORMAL;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "reps")
    private Integer reps;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "rpe")
    private Double rpe;

    @Column(name = "is_personal_record", nullable = false)
    private boolean personalRecord = false;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    public WorkoutSet() {
    }

    public UUID getId() {
        return id;
    }

    public WorkoutExercise getWorkoutExercise() {
        return workoutExercise;
    }

    public void setWorkoutExercise(WorkoutExercise workoutExercise) {
        this.workoutExercise = workoutExercise;
    }

    public int getSetOrder() {
        return setOrder;
    }

    public void setSetOrder(int setOrder) {
        this.setOrder = setOrder;
    }

    public SetType getSetType() {
        return setType;
    }

    public void setSetType(SetType setType) {
        this.setType = setType;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getRpe() {
        return rpe;
    }

    public void setRpe(Double rpe) {
        this.rpe = rpe;
    }

    public boolean isPersonalRecord() {
        return personalRecord;
    }

    public void setPersonalRecord(boolean personalRecord) {
        this.personalRecord = personalRecord;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}