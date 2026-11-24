package com.fitstud.backend.dto.routine;

import java.util.UUID;

public record RoutineExerciseResponse(
        UUID id,
        UUID exerciseId,
        String exerciseName,
        int position,
        String notes
) {}