
package com.fitstud.backend.controller;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.enums.MuscleGroup;
import com.fitstud.backend.service.ExerciseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<Exercise> getBuiltInExercises() {
        return exerciseService.getBuiltInExercises();
    }

    @GetMapping("/by-muscle/{muscleGroup}")
    public List<Exercise> getByPrimaryMuscle(
            @PathVariable MuscleGroup muscleGroup) {
        return exerciseService.getByPrimaryMuscle(muscleGroup);
    }
}
