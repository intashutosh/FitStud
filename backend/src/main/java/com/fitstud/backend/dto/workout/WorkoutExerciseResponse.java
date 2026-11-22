package com.fitstud.backend.dto.workout;

import java.util.UUID;

public record WorkoutExerciseResponse(
        UUID id,
        UUID exerciseId,
        String exerciseName,
        int position,
        String notes
) {}