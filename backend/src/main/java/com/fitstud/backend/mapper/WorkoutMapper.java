package com.fitstud.backend.mapper;

import com.fitstud.backend.dto.workout.WorkoutResponse;
import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.dto.workout.WorkoutExerciseResponse;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.dto.workout.WorkoutSetResponse;
import com.fitstud.backend.entity.WorkoutSet;
import com.fitstud.backend.dto.workout.WorkoutExerciseDetailsResponse;
import java.util.List;
import com.fitstud.backend.dto.workout.WorkoutDetailsResponse;
import org.springframework.stereotype.Component;


@Component
public class WorkoutMapper {

    public WorkoutResponse toWorkoutResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getSourceRoutine() != null
                        ? workout.getSourceRoutine().getId()
                        : null,
                workout.getStartedAt(),
                workout.getFinishedAt(),
                workout.getStatus(),
                workout.getSessionMode(),
                workout.getVisibility(),
                workout.getDurationSeconds(),
                workout.getTotalVolumeKg(),
                workout.getTotalSets(),
                workout.getPrCount()
        );
    }

    public WorkoutExerciseResponse toWorkoutExerciseResponse(
            WorkoutExercise workoutExercise
    ) {
        return new WorkoutExerciseResponse(
                workoutExercise.getId(),
                workoutExercise.getExercise().getId(),
                workoutExercise.getExercise().getName(),
                workoutExercise.getPosition(),
                workoutExercise.getNotes()
        );
    }

    public WorkoutSetResponse toWorkoutSetResponse(WorkoutSet workoutSet) {
        return new WorkoutSetResponse(
                workoutSet.getId(),
                workoutSet.getSetNumber(),
                workoutSet.getWeightKg(),
                workoutSet.getReps(),
                workoutSet.isCompleted(),
                workoutSet.getCompletedAt()
        );
    }

    public WorkoutExerciseDetailsResponse toWorkoutExerciseDetailsResponse(
            WorkoutExercise workoutExercise,
            List<WorkoutSet> workoutSets
    ) {
        return new WorkoutExerciseDetailsResponse(
                toWorkoutExerciseResponse(workoutExercise),
                workoutSets.stream()
                        .map(this::toWorkoutSetResponse)
                        .toList()
        );
    }

    public WorkoutDetailsResponse toWorkoutDetailsResponse(
            Workout workout,
            List<WorkoutExerciseDetailsResponse> exercises
    ) {
        return new WorkoutDetailsResponse(
                toWorkoutResponse(workout),
                exercises
        );
    }

}