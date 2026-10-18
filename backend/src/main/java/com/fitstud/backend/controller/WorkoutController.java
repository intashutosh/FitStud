package com.fitstud.backend.controller;

import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.entity.WorkoutSet;
import com.fitstud.backend.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    // POST /api/workouts — Start a new workout
    @PostMapping
    public ResponseEntity<Workout> startWorkout(@RequestBody Workout workout) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.startWorkout(workout));
    }

    // GET /api/workouts/{id} — Get full workout details with exercises and sets
    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkout(@PathVariable UUID id) {
        return ResponseEntity.ok(workoutService.getWorkoutById(id));
    }

    // GET /api/workouts/user/{userId} — Get a user's workout history
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Workout>> getUserWorkouts(@PathVariable UUID userId) {
        return ResponseEntity.ok(workoutService.getUserWorkouts(userId));
    }

    // GET /api/workouts/feed — Get public completed workouts for social feed
    @GetMapping("/feed")
    public ResponseEntity<List<Workout>> getPublicFeed() {
        return ResponseEntity.ok(workoutService.getPublicFeed());
    }

    // POST /api/workouts/{workoutId}/exercises — Add an exercise to a workout
    @PostMapping("/{workoutId}/exercises")
    public ResponseEntity<WorkoutExercise> addExerciseToWorkout(
            @PathVariable UUID workoutId,
            @RequestBody Map<String, Object> payload
    ) {
        UUID exerciseId = UUID.fromString(payload.get("exerciseId").toString());
        Integer restTimerSeconds = payload.get("restTimerSeconds") != null
                ? Integer.valueOf(payload.get("restTimerSeconds").toString()) : 90;
        Integer supersetGroupId = payload.get("supersetGroupId") != null
                ? Integer.valueOf(payload.get("supersetGroupId").toString()) : null;
        String notes = payload.get("notes") != null ? payload.get("notes").toString() : null;

        WorkoutExercise created = workoutService.addExerciseToWorkout(
                workoutId, exerciseId, restTimerSeconds, supersetGroupId, notes
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // POST /api/workouts/exercises/{workoutExerciseId}/sets — Log a new set
    @PostMapping("/exercises/{workoutExerciseId}/sets")
    public ResponseEntity<WorkoutSet> addSet(
            @PathVariable UUID workoutExerciseId,
            @RequestBody WorkoutSet workoutSet
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.addSetToExercise(workoutExerciseId, workoutSet));
    }

    // PUT /api/workouts/sets/{setId} — Update weight/reps/RPE/completed on a set
    @PutMapping("/sets/{setId}")
    public ResponseEntity<WorkoutSet> updateSet(
            @PathVariable UUID setId,
            @RequestBody WorkoutSet workoutSet
    ) {
        return ResponseEntity.ok(workoutService.updateSet(setId, workoutSet));
    }

    // POST /api/workouts/{id}/complete — Finish workout & calculate volume/stats
    @PostMapping("/{id}/complete")
    public ResponseEntity<Workout> completeWorkout(@PathVariable UUID id) {
        return ResponseEntity.ok(workoutService.completeWorkout(id));
    }

    // POST /api/workouts/{id}/discard — Discard workout
    @PostMapping("/{id}/discard")
    public ResponseEntity<Workout> discardWorkout(@PathVariable UUID id) {
        return ResponseEntity.ok(workoutService.discardWorkout(id));
    }
}