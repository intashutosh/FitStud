package com.fitstud.backend.repository;

import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    List<Workout> findByUser_IdOrderByStartedAtDesc(UUID userId);

    Optional<Workout> findByIdAndUser_Id(UUID workoutId, UUID userId);

    List<Workout> findByUser_IdAndStatusOrderByStartedAtDesc(
            UUID userId,
            WorkoutStatus status
    );

    boolean existsByIdAndUser_IdAndStatus(
            UUID workoutId,
            UUID userId,
            WorkoutStatus status
    );
}