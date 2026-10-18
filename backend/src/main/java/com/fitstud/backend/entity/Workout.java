package com.fitstud.backend.entity;

import com.fitstud.backend.enums.SessionMode;
import com.fitstud.backend.enums.Visibility;
import com.fitstud.backend.enums.WorkoutStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workouts", indexes = {
        @Index(name = "idx_workout_user_started", columnList = "user_id, started_at"),
        @Index(name = "idx_workout_user_status", columnList = "user_id, status"),
        @Index(name = "idx_workout_feed", columnList = "status, visibility, completed_at")
})
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Loose UUID reference to the Routine template.
     * Ensures deleting a future routine never deletes or breaks historical workout logs.
     */
    @Column(name = "source_routine_id")
    private UUID sourceRoutineId;

    @Column(nullable = false, length = 120)
    @Size(max = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutStatus status = WorkoutStatus.IN_PROGRESS;

    // --- ADAPTIVE PLANNER METADATA ---

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false, length = 30)
    private SessionMode sessionMode = SessionMode.STANDARD;

    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    // --- TIMESTAMPS ---

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds = 0L;

    // --- DENORMALIZED SUMMARY STATS (For Fast Social Feeds & Analytics) ---

    @Column(name = "total_volume_kg", nullable = false)
    private Double totalVolumeKg = 0.0;

    @Column(name = "total_sets", nullable = false)
    private Integer totalSets = 0;

    @Column(name = "pr_count", nullable = false)
    private Integer prCount = 0;

    // --- SOCIAL & MEDIA ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(length = 1000)
    @Size(max = 1000)
    private String notes;

    @Column(name = "media_url")
    private String mediaUrl;

    public Workout() {
    }

    @PrePersist
    protected void onCreate() {
        // Only default to now() if the user didn't backdate the workout
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }

    /**
     * Domain helper to cleanly finalize a workout session and cache feed metrics.
     */
    public void completeWorkout(LocalDateTime finishedTime, double volumeKg, int completedSets, int prs) {
        this.status = WorkoutStatus.COMPLETED;
        this.completedAt = (finishedTime != null) ? finishedTime : LocalDateTime.now();
        if (this.durationSeconds == null || this.durationSeconds == 0L) {
            this.durationSeconds = Math.max(0L, Duration.between(this.startedAt, this.completedAt).getSeconds());
        }
        this.totalVolumeKg = volumeKg;
        this.totalSets = completedSets;
        this.prCount = prs;
    }



    // --- GETTERS & SETTERS ---

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSourceRoutineId() {
        return sourceRoutineId;
    }

    public void setSourceRoutineId(UUID sourceRoutineId) {
        this.sourceRoutineId = sourceRoutineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkoutStatus getStatus() {
        return status;
    }

    public void setStatus(WorkoutStatus status) {
        this.status = status;
    }

    public SessionMode getSessionMode() {
        return sessionMode;
    }

    public void setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode;
    }

    public Integer getTargetDurationMinutes() {
        return targetDurationMinutes;
    }

    public void setTargetDurationMinutes(Integer targetDurationMinutes) {
        this.targetDurationMinutes = targetDurationMinutes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getTotalVolumeKg() {
        return totalVolumeKg;
    }

    public void setTotalVolumeKg(Double totalVolumeKg) {
        this.totalVolumeKg = totalVolumeKg;
    }

    public Integer getTotalSets() {
        return totalSets;
    }

    public void setTotalSets(Integer totalSets) {
        this.totalSets = totalSets;
    }

    public Integer getPrCount() {
        return prCount;
    }

    public void setPrCount(Integer prCount) {
        this.prCount = prCount;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<WorkoutExercise> exercises = new ArrayList<>();

    public List<WorkoutExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExercise> exercises) {
        this.exercises = exercises;
    }
}