
package com.fitstud.backend.repository;

import com.fitstud.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseRepository
        extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByPrimaryMuscle(
            com.fitstud.backend.enums.MuscleGroup primaryMuscle);

    List<Exercise> findByCustomFalse();

    List<Exercise> findByCustomTrueAndCreatedByUserId(
            UUID createdByUserId);
}
