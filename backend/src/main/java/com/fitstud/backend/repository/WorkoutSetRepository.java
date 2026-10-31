package com.fitstud.backend.repository;

import com.fitstud.backend.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {

    List<WorkoutSet> findByWorkoutExercise_IdOrderBySetNumberAsc(
            UUID workoutExerciseId
    );

    Optional<WorkoutSet> findByIdAndWorkoutExercise_Id(
            UUID setId,
            UUID workoutExerciseId
    );

    boolean existsByWorkoutExercise_IdAndSetNumber(
            UUID workoutExerciseId,
            int setNumber
    );

    long countByWorkoutExercise_Id(UUID workoutExerciseId);

    long countByWorkoutExercise_IdAndCompletedTrue(UUID workoutExerciseId);

}