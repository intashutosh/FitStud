package com.fitstud.backend.dto.workout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReorderWorkoutExerciseRequest(
        @NotNull
        @Min(0)
        Integer newPosition
) {}