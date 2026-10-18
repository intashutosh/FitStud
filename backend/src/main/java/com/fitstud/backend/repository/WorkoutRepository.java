package com.fitstud.backend.repository;

import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.enums.Visibility;
import com.fitstud.backend.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    // User's workout history ordered newest first
    List<Workout> findByUserIdOrderByStartedAtDesc(UUID userId);

    // Filter a user's workouts by status (e.g., check if they have an active IN_PROGRESS workout)
    List<Workout> findByUserIdAndStatusOrderByStartedAtDesc(UUID userId, WorkoutStatus status);

    // Quickly fetch the single active workout for a user
    Optional<Workout> findFirstByUserIdAndStatusOrderByStartedAtDesc(UUID userId, WorkoutStatus status);

    // Global / Community Social Feed query
    List<Workout> findByStatusAndVisibilityOrderByCompletedAtDesc(WorkoutStatus status, Visibility visibility);
}