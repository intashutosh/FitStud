package com.fitstud.backend.repository;

import com.fitstud.backend.entity.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineExerciseRepository
        extends JpaRepository<RoutineExercise, UUID> {

    // Get all exercises in a routine, ordered by their position.
    List<RoutineExercise> findByRoutine_IdOrderByPositionAsc(UUID routineId);

    // Find an exercise entry within a specific routine.
    Optional<RoutineExercise> findByIdAndRoutine_Id(
            UUID id,
            UUID routineId
    );

    // Check whether a position is already occupied in a routine.
    boolean existsByRoutine_IdAndPosition(UUID routineId, int position);

    // Count the number of exercises in a routine.
    long countByRoutine_Id(UUID routineId);
}