package com.fitstud.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workout_exercises", indexes = {
        @Index(name = "idx_we_workout_order", columnList = "workout_id, order_index"),
        @Index(name = "idx_we_exercise", columnList = "exercise_id")
})
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "superset_group_id")
    private Integer supersetGroupId;

    @Column(name = "rest_timer_seconds")
    private Integer restTimerSeconds = 90;

    @Column(length = 500)
    @Size(max = 500)
    private String notes;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setOrder ASC")
    private List<WorkoutSet> sets = new ArrayList<>();

    public WorkoutExercise() {
    }

    public UUID getId() {
        return id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getSupersetGroupId() {
        return supersetGroupId;
    }

    public void setSupersetGroupId(Integer supersetGroupId) {
        this.supersetGroupId = supersetGroupId;
    }

    public Integer getRestTimerSeconds() {
        return restTimerSeconds;
    }

    public void setRestTimerSeconds(Integer restTimerSeconds) {
        this.restTimerSeconds = restTimerSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<WorkoutSet> getSets() {
        return sets;
    }

    public void setSets(List<WorkoutSet> sets) {
        this.sets = sets;
    }
}