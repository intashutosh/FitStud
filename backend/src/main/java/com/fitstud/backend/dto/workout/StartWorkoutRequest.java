package com.fitstud.backend.dto.workout;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartWorkoutRequest(
        @NotNull UUID routineId
) {}