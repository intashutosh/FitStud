package com.fitstud.backend.mapper;

import com.fitstud.backend.dto.routine.*;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoutineMapper {

    public RoutineResponse toRoutineResponse(Routine routine) {
        return new RoutineResponse(
                routine.getId(),
                routine.getName(),
                routine.getDescription(),
                routine.getCreatedAt(),
                routine.getUpdatedAt()
        );
    }

    public RoutineExerciseResponse toExerciseResponse(
            RoutineExercise routineExercise) {

        return new RoutineExerciseResponse(
                routineExercise.getId(),
                routineExercise.getExercise().getId(),
                routineExercise.getExercise().getName(),
                routineExercise.getPosition(),
                routineExercise.getNotes()
        );
    }

    public RoutineDetailsResponse toDetailsResponse(
            Routine routine,
            List<RoutineExercise> exercises) {

        return new RoutineDetailsResponse(
                toRoutineResponse(routine),
                exercises.stream()
                        .map(this::toExerciseResponse)
                        .toList()
        );
    }
}