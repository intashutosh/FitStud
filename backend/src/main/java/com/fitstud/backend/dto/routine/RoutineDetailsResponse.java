package com.fitstud.backend.dto.routine;

import java.util.List;

public record RoutineDetailsResponse(
        RoutineResponse routine,
        List<RoutineExerciseResponse> exercises
) {}