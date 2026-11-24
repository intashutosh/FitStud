package com.fitstud.backend.mapper;

import com.fitstud.backend.dto.exercise.ExerciseResponse;
import com.fitstud.backend.entity.Exercise;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getMuscleGroup(),
                exercise.getEquipment(),
                exercise.isGlobal(),
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
        );
    }
}