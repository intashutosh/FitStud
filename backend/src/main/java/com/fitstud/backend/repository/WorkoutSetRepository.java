package com.fitstud.backend.repository;

import com.fitstud.backend.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {

    List<WorkoutSet> findByWorkoutExerciseIdOrderBySetOrderAsc(UUID workoutExerciseId);
}