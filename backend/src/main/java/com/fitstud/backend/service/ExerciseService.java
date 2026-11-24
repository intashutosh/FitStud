package com.fitstud.backend.service;

import com.fitstud.backend.dto.exercise.*;
import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.mapper.ExerciseMapper;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final ExerciseMapper exerciseMapper;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            UserRepository userRepository,
            ExerciseMapper exerciseMapper
    ) {
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.exerciseMapper = exerciseMapper;
    }

    // Get global exercises and the current user's custom exercises.
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExerciseCatalog(UUID userId) {

        List<Exercise> globalExercises =
                exerciseRepository.findByOwnerIsNullOrderByNameAsc();

        List<Exercise> customExercises =
                exerciseRepository.findByOwner_IdOrderByNameAsc(userId);

        return Stream.concat(
                        globalExercises.stream(),
                        customExercises.stream()
                )
                .sorted(Comparator.comparing(
                        Exercise::getName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(exerciseMapper::toResponse)
                .toList();
    }

    // Get one exercise, provided it is global or belongs to the user.
    @Transactional(readOnly = true)
    public ExerciseResponse getExercise(UUID userId, UUID exerciseId) {

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exercise not found"));

        validateVisibility(exercise, userId);

        return exerciseMapper.toResponse(exercise);
    }

    // Create a custom exercise.
    @Transactional
    public ExerciseResponse createExercise(
            UUID userId,
            CreateExerciseRequest request
    ) {

        String name = request.name().trim();

        if (exerciseRepository.existsByOwner_IdAndNameIgnoreCase(
                userId, name)) {
            throw new IllegalArgumentException(
                    "You already have a custom exercise with this name");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Exercise exercise = Exercise.createCustom(
                owner,
                name,
                request.description(),
                request.muscleGroup(),
                request.equipment()
        );

        exercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(exercise);
    }

    // Update a custom exercise.
    @Transactional
    public ExerciseResponse updateExercise(
            UUID userId,
            UUID exerciseId,
            UpdateExerciseRequest request
    ) {

        Exercise exercise = exerciseRepository
                .findByIdAndOwner_Id(exerciseId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Custom exercise not found"));

        String name = request.name().trim();

        if (exerciseRepository
                .existsByOwner_IdAndNameIgnoreCaseAndIdNot(
                        userId, name, exerciseId)) {
            throw new IllegalArgumentException(
                    "You already have a custom exercise with this name");
        }

        exercise.setName(name);
        exercise.setDescription(request.description());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setEquipment(request.equipment());

        return exerciseMapper.toResponse(exerciseRepository.saveAndFlush(exercise));
    }

    // Delete a custom exercise.
    @Transactional
    public void deleteExercise(UUID userId, UUID exerciseId) {

        Exercise exercise = exerciseRepository
                .findByIdAndOwner_Id(exerciseId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Custom exercise not found"));

        try {
            exerciseRepository.delete(exercise);
            exerciseRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Cannot delete this exercise because it is used in a routine or workout"
            );
        }
    }

    private void validateVisibility(Exercise exercise, UUID userId) {

        if (!exercise.isGlobal()
                && !exercise.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Exercise not found");
        }
    }
}