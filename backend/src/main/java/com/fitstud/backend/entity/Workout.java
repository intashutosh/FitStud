package com.fitstud.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.enums.SessionMode;
import com.fitstud.backend.enums.WorkoutVisibility;

@Entity
@Table(
        name = "workouts",
        indexes = {
                @Index(
                        name = "idx_workouts_user_started",
                        columnList = "user_id, started_at"
                ),
                @Index(
                        name = "idx_workouts_user_status",
                        columnList = "user_id, status"
                ),
                @Index(
                        name = "idx_workouts_source_routine",
                        columnList = "source_routine_id"
                )
        }
)
public class Workout {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_workouts_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_routine_id",
            foreignKey = @ForeignKey(name = "fk_workouts_source_routine")
    )
    private Routine sourceRoutine;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false, length = 40)
    private SessionMode sessionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkoutVisibility visibility;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_volume_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVolumeKg = BigDecimal.ZERO;

    @Column(name = "total_sets", nullable = false)
    private int totalSets;

    @Column(name = "pr_count", nullable = false)
    private int prCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Workout() {
    }

    public static Workout start(User user, Routine sourceRoutine) {
        if (user == null) {
            throw new IllegalArgumentException("Workout owner cannot be null");
        }

        Workout workout = new Workout();
        workout.user = user;
        workout.sourceRoutine = sourceRoutine;
        workout.status = WorkoutStatus.IN_PROGRESS;
        workout.sessionMode = SessionMode.STANDARD;
        workout.visibility = WorkoutVisibility.PRIVATE;
        workout.totalVolumeKg = BigDecimal.ZERO;
        workout.totalSets = 0;
        workout.prCount = 0;

        return workout;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();

        if (startedAt == null) {
            startedAt = now;
        }

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

    public User getUser() {
        return user;
    }

    public Routine getSourceRoutine() {
        return sourceRoutine;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public WorkoutStatus getStatus() {
        return status;
    }

    public SessionMode getSessionMode() {
        return sessionMode;
    }

    public WorkoutVisibility getVisibility() {
        return visibility;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public BigDecimal getTotalVolumeKg() {
        return totalVolumeKg;
    }

    public int getTotalSets() {
        return totalSets;
    }

    public int getPrCount() {
        return prCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}