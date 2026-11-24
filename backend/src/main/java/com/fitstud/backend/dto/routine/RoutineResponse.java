package com.fitstud.backend.dto.routine;

import java.time.Instant;
import java.util.UUID;

public record RoutineResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}