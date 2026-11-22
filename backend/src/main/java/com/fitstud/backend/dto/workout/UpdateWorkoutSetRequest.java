package com.fitstud.backend.dto.workout;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateWorkoutSetRequest(

        @DecimalMin(value = "0.0")
        BigDecimal weightKg,

        @Min(1)
        Integer reps

) {}