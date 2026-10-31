package com.fitstud.backend.service;

import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.repository.RoutineRepository;
import com.fitstud.backend.repository.UserRepository;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.RoutineExerciseRepository;

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

    public RoutineService(
            RoutineRepository routineRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository,
            RoutineExerciseRepository routineExerciseRepository
    ) {
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.routineExerciseRepository = routineExerciseRepository;
    }

    @Transactional
    public Routine createRoutine(
            UUID userId,
            String name,
            String description
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        Routine routine = Routine.create(user, name, description);

        return routineRepository.save(routine);
    }

    @Transactional(readOnly = true)
    public Routine getMyRoutine(UUID userId, UUID routineId) {
        return routineRepository
                .findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Routine not found"
                        )
                );
    }

    @Transactional
    public RoutineExercise addExercise(
            UUID userId,
            UUID routineId,
            UUID exerciseId,
            int position,
            String notes
    ) {
        // 1. Verify that the routine belongs to this user.
        Routine routine = routineRepository
                .findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Routine not found")
                );

        // 2. Validate the requested position.
        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position cannot be negative"
            );
        }

        // 3. Prevent duplicate positions within the routine.
        if (routineExerciseRepository
                .existsByRoutine_IdAndPosition(routineId, position)) {
            throw new IllegalArgumentException(
                    "This position is already occupied"
            );
        }

        // 4. Find the requested exercise.
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exercise not found")
                );

        // 5. Allow global exercises or exercises owned by this user.
        boolean isGlobal = exercise.getOwner() == null;

        boolean isOwnedByUser = exercise.getOwner() != null
                && exercise.getOwner().getId().equals(userId);

        if (!isGlobal && !isOwnedByUser) {
            throw new IllegalArgumentException("Exercise not found");
        }

        // 6. Create and persist the routine-exercise relationship.
        RoutineExercise routineExercise = RoutineExercise.create(
                routine,
                exercise,
                position,
                notes
        );

        return routineExerciseRepository.save(routineExercise);
    }


    @Transactional(readOnly = true)
    public List<RoutineExercise> getRoutineExercises(
            UUID userId,
            UUID routineId
    ) {
        // Verify ownership before returning any exercises.
        routineRepository.findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Routine not found")
                );

        return routineExerciseRepository
                .findByRoutine_IdOrderByPositionAsc(routineId);
    }
}