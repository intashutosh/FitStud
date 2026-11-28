package com.fitstud.backend.dto.routinefolder;

import java.time.Instant;
import java.util.UUID;

public record RoutineFolderResponse(
        UUID id,
        String name,
        int position,
        long routineCount,
        Instant createdAt,
        Instant updatedAt
) {
}