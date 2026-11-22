package com.fitstud.backend.dto.workout;

import java.util.List;

public record WorkoutExerciseDetailsResponse(
        WorkoutExerciseResponse exercise,
        List<WorkoutSetResponse> sets
) {}