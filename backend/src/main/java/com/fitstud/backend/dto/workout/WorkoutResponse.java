package com.fitstud.backend.dto.workout;

import com.fitstud.backend.enums.SessionMode;
import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.enums.WorkoutVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkoutResponse(
        UUID id,
        UUID sourceRoutineId,
        Instant startedAt,
        Instant finishedAt,
        WorkoutStatus status,
        SessionMode sessionMode,
        WorkoutVisibility visibility,
        Integer durationSeconds,
        BigDecimal totalVolumeKg,
        int totalSets,
        int prCount
) {}