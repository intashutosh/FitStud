package com.fitstud.backend.dto.exercise;

import java.time.Instant;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        String muscleGroup,
        String equipment,
        boolean global,
        Instant createdAt,
        Instant updatedAt
) {}