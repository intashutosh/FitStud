package com.fitstud.backend.repository;

import com.fitstud.backend.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutExerciseRepository
        extends JpaRepository<WorkoutExercise, UUID> {

    List<WorkoutExercise> findByWorkout_IdOrderByPositionAsc(UUID workoutId);

    Optional<WorkoutExercise> findByIdAndWorkout_Id(
            UUID id,
            UUID workoutId
    );

    boolean existsByWorkout_IdAndPosition(
            UUID workoutId,
            int position
    );

    long countByWorkout_Id(UUID workoutId);

    boolean existsByWorkout_IdAndExercise_Id(
            UUID workoutId,
            UUID exerciseId
    );
}