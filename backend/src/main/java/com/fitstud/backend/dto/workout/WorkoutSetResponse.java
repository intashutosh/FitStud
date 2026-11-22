package com.fitstud.backend.dto.workout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkoutSetResponse(
        UUID id,
        int setNumber,
        BigDecimal weightKg,
        Integer reps,
        boolean completed,
        Instant completedAt
) {}