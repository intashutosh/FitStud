package com.fitstud.backend.dto.workout;

import java.util.List;

public record WorkoutDetailsResponse(
        WorkoutResponse workout,
        List<WorkoutExerciseDetailsResponse> exercises
) {}