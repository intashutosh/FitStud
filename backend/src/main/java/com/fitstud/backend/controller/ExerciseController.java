package com.fitstud.backend.controller;

import com.fitstud.backend.dto.exercise.*;
import com.fitstud.backend.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    private UUID getUserId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }

    @GetMapping
    public List<ExerciseResponse> getExerciseCatalog(
            @AuthenticationPrincipal UserDetails userDetails) {

        return exerciseService.getExerciseCatalog(getUserId(userDetails));
    }

    @GetMapping("/{exerciseId}")
    public ExerciseResponse getExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID exerciseId) {

        return exerciseService.getExercise(
                getUserId(userDetails), exerciseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateExerciseRequest request) {

        return exerciseService.createExercise(
                getUserId(userDetails), request);
    }

    @PutMapping("/{exerciseId}")
    public ExerciseResponse updateExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID exerciseId,
            @Valid @RequestBody UpdateExerciseRequest request) {

        return exerciseService.updateExercise(
                getUserId(userDetails), exerciseId, request);
    }

    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID exerciseId) {

        exerciseService.deleteExercise(
                getUserId(userDetails), exerciseId);
    }
}