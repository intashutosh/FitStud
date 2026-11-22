package com.fitstud.backend.dto.workout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddWorkoutExerciseRequest(

        @NotNull
        UUID exerciseId,

        @NotNull
        @Min(0)
        Integer position,

        @Size(max = 300)
        String notes

) {}