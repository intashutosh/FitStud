package com.fitstud.backend.dto.routine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoutineRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {}