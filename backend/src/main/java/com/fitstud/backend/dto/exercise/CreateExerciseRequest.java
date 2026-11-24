package com.fitstud.backend.dto.exercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExerciseRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Size(max = 50)
        String muscleGroup,

        @Size(max = 50)
        String equipment

) {}