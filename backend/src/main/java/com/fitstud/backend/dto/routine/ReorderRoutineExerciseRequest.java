package com.fitstud.backend.dto.routine;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReorderRoutineExerciseRequest(
        @NotNull
        @Min(0)
        Integer newPosition
) {}