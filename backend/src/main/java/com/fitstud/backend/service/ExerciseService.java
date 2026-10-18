
package com.fitstud.backend.service;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.enums.MuscleGroup;
import com.fitstud.backend.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional(readOnly = true)
    public List<Exercise> getBuiltInExercises() {
        return exerciseRepository.findByCustomFalse();
    }

    @Transactional(readOnly = true)
    public List<Exercise> getMyCustomExercises(UUID userId) {
        return exerciseRepository
                .findByCustomTrueAndCreatedByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Exercise> getByPrimaryMuscle(MuscleGroup muscleGroup) {
        return exerciseRepository.findByPrimaryMuscle(muscleGroup);
    }
}
