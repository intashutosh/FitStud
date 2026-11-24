package com.fitstud.backend.service;

import com.fitstud.backend.dto.routine.*;
import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.exception.ResourceNotFoundException;
import com.fitstud.backend.mapper.RoutineMapper;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.RoutineExerciseRepository;
import com.fitstud.backend.repository.RoutineRepository;
import com.fitstud.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineMapper routineMapper;

    public RoutineService(
            RoutineRepository routineRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository,
            RoutineExerciseRepository routineExerciseRepository,
            RoutineMapper routineMapper
    ) {
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.routineExerciseRepository = routineExerciseRepository;
        this.routineMapper = routineMapper;
    }

    // Create a routine.
    @Transactional
    public RoutineResponse createRoutine(
            UUID userId,
            CreateRoutineRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Routine routine = Routine.create(
                user,
                request.name(),
                request.description()
        );

        routine = routineRepository.saveAndFlush(routine);

        return routineMapper.toRoutineResponse(routine);
    }

    // Get all routines belonging to the user.
    @Transactional(readOnly = true)
    public List<RoutineResponse> getMyRoutines(UUID userId) {

        return routineRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(routineMapper::toRoutineResponse)
                .toList();
    }

    // Get one routine with all its exercises.
    @Transactional(readOnly = true)
    public RoutineDetailsResponse getRoutineDetails(
            UUID userId,
            UUID routineId
    ) {
        Routine routine = findOwnedRoutine(userId, routineId);

        List<RoutineExercise> exercises =
                routineExerciseRepository
                        .findByRoutine_IdOrderByPositionAsc(routineId);

        return routineMapper.toDetailsResponse(routine, exercises);
    }

    // Update routine name and description.
    @Transactional
    public RoutineResponse updateRoutine(
            UUID userId,
            UUID routineId,
            UpdateRoutineRequest request
    ) {
        Routine routine = findOwnedRoutine(userId, routineId);

        routine.setName(request.name());
        routine.setDescription(request.description());

        routine = routineRepository.saveAndFlush(routine);

        return routineMapper.toRoutineResponse(routine);
    }

    // Delete a routine.
    @Transactional
    public void deleteRoutine(UUID userId, UUID routineId) {

        Routine routine = findOwnedRoutine(userId, routineId);

        routineRepository.delete(routine);
        routineRepository.flush();
    }

    // Add an exercise to the end of a routine.
    @Transactional
    public RoutineExerciseResponse addExercise(
            UUID userId,
            UUID routineId,
            UUID exerciseId,
            int position,
            String notes
    ) {
        Routine routine = findOwnedRoutine(userId, routineId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Exercise not found"));

        validateExerciseOwnership(exercise, userId);

        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position cannot be negative");
        }

        long exerciseCount =
                routineExerciseRepository.countByRoutine_Id(routineId);

        if (position != exerciseCount) {
            throw new IllegalArgumentException(
                    "New exercise must be added at the next available position");
        }

        if (routineExerciseRepository
                .existsByRoutine_IdAndExercise_Id(routineId, exerciseId)) {
            throw new IllegalArgumentException(
                    "Exercise already exists in this routine");
        }

        RoutineExercise routineExercise = RoutineExercise.create(
                routine,
                exercise,
                position,
                notes
        );

        routineExercise = routineExerciseRepository.saveAndFlush(
                routineExercise);

        return routineMapper.toExerciseResponse(routineExercise);
    }

    // Remove an exercise from a routine.
    @Transactional
    public void removeExercise(
            UUID userId,
            UUID routineId,
            UUID exerciseId
    ) {
        findOwnedRoutine(userId, routineId);

        RoutineExercise routineExercise =
                routineExerciseRepository
                        .findByRoutine_IdAndExercise_Id(
                                routineId, exerciseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Exercise not found in this routine"));

        routineExerciseRepository.delete(routineExercise);
        routineExerciseRepository.flush();

        compactExercisePositions(routineId);
    }

    // Reorder exercises within a routine.
    @Transactional
    public void reorderExercise(
            UUID userId,
            UUID routineId,
            UUID exerciseId,
            int newPosition
    ) {
        findOwnedRoutine(userId, routineId);

        List<RoutineExercise> exercises =
                routineExerciseRepository
                        .findByRoutine_IdOrderByPositionAsc(routineId);

        if (newPosition < 0 || newPosition >= exercises.size()) {
            throw new IllegalArgumentException(
                    "New position is outside the valid range");
        }

        RoutineExercise target = exercises.stream()
                .filter(item ->
                        item.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exercise not found in this routine"));

        int oldPosition = target.getPosition();

        if (oldPosition == newPosition) {
            return;
        }

        exercises.remove(target);
        exercises.add(newPosition, target);

        // Move all exercises to temporary positions first.
        int offset = exercises.stream()
                .mapToInt(RoutineExercise::getPosition)
                .max()
                .orElse(0) + 1;

        for (RoutineExercise exercise : exercises) {
            exercise.setPosition(exercise.getPosition() + offset);
        }

        routineExerciseRepository.flush();

        // Assign final contiguous positions.
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setPosition(i);
        }
    }

    // Shared helper: retrieve only a routine owned by this user.
    private Routine findOwnedRoutine(UUID userId, UUID routineId) {

        return routineRepository
                .findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Routine not found"));
    }

    // Shared helper: global exercises or the user's own custom exercises.
    private void validateExerciseOwnership(
            Exercise exercise,
            UUID userId
    ) {
        if (!exercise.isGlobal()
                && !exercise.getOwner().getId().equals(userId)) {
            throw new ResourceNotFoundException("Exercise not found");
        }
    }

    // Compact positions after removing an exercise.
    private void compactExercisePositions(UUID routineId) {

        List<RoutineExercise> exercises =
                routineExerciseRepository
                        .findByRoutine_IdOrderByPositionAsc(routineId);

        if (exercises.isEmpty()) {
            return;
        }

        int offset = exercises.stream()
                .mapToInt(RoutineExercise::getPosition)
                .max()
                .orElse(0) + 1;

        for (RoutineExercise exercise : exercises) {
            exercise.setPosition(exercise.getPosition() + offset);
        }

        routineExerciseRepository.flush();

        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setPosition(i);
        }
    }
}