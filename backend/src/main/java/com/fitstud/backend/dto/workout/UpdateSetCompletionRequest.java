package com.fitstud.backend.dto.workout;

import jakarta.validation.constraints.NotNull;

public record UpdateSetCompletionRequest(
        @NotNull Boolean completed
) {}