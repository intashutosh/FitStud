
package com.fitstud.backend.controller;

import com.fitstud.backend.dto.workout.*;
import com.fitstud.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    private UUID getUserId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse startBlankWorkout(
            @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.startBlankWorkout(getUserId(userDetails));
    }

    @PostMapping("/from-routine/{routineId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse startFromRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId) {
        return workoutService.startWorkoutFromRoutine(
                getUserId(userDetails), routineId);
    }

    @GetMapping
    public List<WorkoutResponse> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.getWorkoutHistoryResponses(getUserId(userDetails));
    }

    @GetMapping("/ongoing")
    public List<WorkoutResponse> getOngoing(
            @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.getOngoingWorkoutResponses(getUserId(userDetails));
    }

    @GetMapping("/{workoutId}")
    public WorkoutDetailsResponse getWorkoutDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId) {
        return workoutService.getWorkoutDetails(getUserId(userDetails), workoutId);
    }

    @PostMapping("/{workoutId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutExerciseResponse addExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @Valid @RequestBody AddWorkoutExerciseRequest request) {
        return workoutService.addExercise(
                getUserId(userDetails), workoutId,
                request.exerciseId(), request.position(), request.notes());
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId) {
        workoutService.deleteExercise(
                getUserId(userDetails), workoutId, exerciseId);
    }

    @PutMapping("/{workoutId}/exercises/{exerciseId}/position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId,
            @Valid @RequestBody ReorderWorkoutExerciseRequest request) {
        workoutService.reorderExercise(
                getUserId(userDetails), workoutId,
                exerciseId, request.newPosition());
    }

    @PostMapping("/{workoutId}/exercises/{exerciseId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutSetResponse addSet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId,
            @Valid @RequestBody AddWorkoutSetRequest request) {
        return workoutService.addSet(
                getUserId(userDetails), workoutId, exerciseId,
                request.weightKg(), request.reps());
    }

    @PutMapping("/{workoutId}/exercises/{exerciseId}/sets/{setId}")
    public WorkoutSetResponse updateSet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId,
            @PathVariable UUID setId,
            @Valid @RequestBody UpdateWorkoutSetRequest request) {
        return workoutService.updateSet(
                getUserId(userDetails), workoutId, exerciseId, setId,
                request.weightKg(), request.reps());
    }

    @PatchMapping("/{workoutId}/exercises/{exerciseId}/sets/{setId}/completion")
    public WorkoutSetResponse updateCompletion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId,
            @PathVariable UUID setId,
            @Valid @RequestBody UpdateSetCompletionRequest request) {
        return workoutService.updateSetCompletion(
                getUserId(userDetails), workoutId, exerciseId, setId,
                request.completed());
    }

    @DeleteMapping("/{workoutId}/exercises/{exerciseId}/sets/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId,
            @PathVariable UUID setId) {
        workoutService.deleteSet(
                getUserId(userDetails), workoutId, exerciseId, setId);
    }

    @GetMapping("/{workoutId}/exercises/{exerciseId}/sets")
    public List<WorkoutSetResponse> getSets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId) {
        return workoutService.getWorkoutSets(
                getUserId(userDetails), workoutId, exerciseId);
    }

    @GetMapping("/{workoutId}/exercises/{exerciseId}/previous-performance")
    public List<WorkoutSetResponse> getPreviousPerformance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId,
            @PathVariable UUID exerciseId) {
        return workoutService.getPreviousPerformance(
                getUserId(userDetails), workoutId, exerciseId);
    }

    @PostMapping("/{workoutId}/finish")
    public WorkoutResponse finishWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workoutId) {
        return workoutService.finishWorkout(
                getUserId(userDetails), workoutId);
    }
}