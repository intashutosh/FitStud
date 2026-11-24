
package com.fitstud.backend.controller;

import com.fitstud.backend.dto.routine.*;
import com.fitstud.backend.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    private UUID getUserId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }

    // Create a routine
    @PostMapping
    public ResponseEntity<RoutineResponse> createRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRoutineRequest request) {

        RoutineResponse response = routineService.createRoutine(
                getUserId(userDetails), request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all routines belonging to the current user
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> getMyRoutines(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                routineService.getMyRoutines(getUserId(userDetails))
        );
    }

    // Get routine details, including its exercises
    @GetMapping("/{routineId}")
    public ResponseEntity<RoutineDetailsResponse> getRoutineDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId) {

        return ResponseEntity.ok(
                routineService.getRoutineDetails(
                        getUserId(userDetails), routineId
                )
        );
    }

    // Update routine name and description
    @PutMapping("/{routineId}")
    public ResponseEntity<RoutineResponse> updateRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId,
            @Valid @RequestBody UpdateRoutineRequest request) {

        return ResponseEntity.ok(
                routineService.updateRoutine(
                        getUserId(userDetails), routineId, request
                )
        );
    }

    // Delete a routine
    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId) {

        routineService.deleteRoutine(getUserId(userDetails), routineId);

        return ResponseEntity.noContent().build();
    }

    // Add an exercise to a routine
    @PostMapping("/{routineId}/exercises")
    public ResponseEntity<RoutineExerciseResponse> addExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId,
            @Valid @RequestBody AddRoutineExerciseRequest request) {

        RoutineExerciseResponse response = routineService.addExercise(
                getUserId(userDetails),
                routineId,
                request.exerciseId(),
                request.position(),
                request.notes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Remove an exercise from a routine
    @DeleteMapping("/{routineId}/exercises/{exerciseId}")
    public ResponseEntity<Void> removeExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId,
            @PathVariable UUID exerciseId) {

        routineService.removeExercise(
                getUserId(userDetails), routineId, exerciseId
        );

        return ResponseEntity.noContent().build();
    }

    // Change an exercise's position in a routine
    @PutMapping("/{routineId}/exercises/{exerciseId}/position")
    public ResponseEntity<Void> reorderExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId,
            @PathVariable UUID exerciseId,
            @Valid @RequestBody ReorderRoutineExerciseRequest request) {

        routineService.reorderExercise(
                getUserId(userDetails),
                routineId,
                exerciseId,
                request.newPosition()
        );

        return ResponseEntity.noContent().build();
    }
}
