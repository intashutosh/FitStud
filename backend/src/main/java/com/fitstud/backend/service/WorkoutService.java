package com.fitstud.backend.service;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.RoutineExerciseRepository;
import com.fitstud.backend.repository.RoutineRepository;
import com.fitstud.backend.repository.UserRepository;
import com.fitstud.backend.repository.WorkoutExerciseRepository;
import com.fitstud.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitstud.backend.entity.WorkoutSet;
import com.fitstud.backend.repository.WorkoutSetRepository;
import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSetRepository workoutSetRepository;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            UserRepository userRepository,
            RoutineRepository routineRepository,
            RoutineExerciseRepository routineExerciseRepository,
            ExerciseRepository exerciseRepository, WorkoutSetRepository workoutSetRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.userRepository = userRepository;
        this.routineRepository = routineRepository;
        this.routineExerciseRepository = routineExerciseRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutSetRepository = workoutSetRepository;
    }

    @Transactional
    public Workout startBlankWorkout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Workout workout = Workout.start(user, null);

        return workoutRepository.save(workout);
    }

    @Transactional
    public Workout startWorkoutFromRoutine(
            UUID userId,
            UUID routineId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Routine routine = routineRepository
                .findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Routine not found"));

        Workout workout = workoutRepository.save(Workout.start(user, routine));

        List<RoutineExercise> routineExercises =
                routineExerciseRepository
                        .findByRoutine_IdOrderByPositionAsc(routineId);

        List<WorkoutExercise> workoutExercises =
                routineExercises.stream()
                        .map(routineExercise ->
                                WorkoutExercise.create(
                                        workout,
                                        routineExercise.getExercise(),
                                        routineExercise.getPosition(),
                                        routineExercise.getNotes()
                                )
                        )
                        .toList();

        workoutExerciseRepository.saveAll(workoutExercises);

        return workout;
    }

    @Transactional
    public WorkoutExercise addExercise(
            UUID userId,
            UUID workoutId,
            UUID exerciseId,
            int position,
            String notes
    ) {
        Workout workout = workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        if (workout.getStatus() != com.fitstud.backend.enums.WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position cannot be negative"
            );
        }

        if (workoutExerciseRepository
                .existsByWorkout_IdAndPosition(workoutId, position)) {
            throw new IllegalArgumentException(
                    "This position is already occupied"
            );
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exercise not found"));

        boolean isGlobal = exercise.getOwner() == null;

        boolean isOwnedByUser = exercise.getOwner() != null
                && exercise.getOwner().getId().equals(userId);

        if (!isGlobal && !isOwnedByUser) {
            throw new IllegalArgumentException("Exercise not found");
        }

        if (workoutExerciseRepository
                .existsByWorkout_IdAndExercise_Id(workoutId, exerciseId)) {
            throw new IllegalArgumentException(
                    "Exercise already exists in this workout"
            );
        }

        WorkoutExercise workoutExercise = WorkoutExercise.create(
                workout,
                exercise,
                position,
                notes
        );

        return workoutExerciseRepository.save(workoutExercise);
    }
    @Transactional(readOnly = true)
    public Workout getMyWorkout(UUID userId, UUID workoutId) {
        return workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));
    }

    @Transactional(readOnly = true)
    public List<WorkoutExercise> getWorkoutExercises(
            UUID userId,
            UUID workoutId
    ) {
        // First verify that this workout belongs to the user.
        workoutRepository.findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        return workoutExerciseRepository
                .findByWorkout_IdOrderByPositionAsc(workoutId);
    }
    @Transactional
    public WorkoutSet addSet(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            BigDecimal weightKg,
            Integer reps
    ) {
        Workout workout = workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        WorkoutExercise workoutExercise = workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout exercise not found"));

        long existingSets = workoutSetRepository
                .countByWorkoutExercise_Id(workoutExerciseId);

        int setNumber = Math.toIntExact(existingSets + 1);

        WorkoutSet workoutSet = WorkoutSet.create(
                workoutExercise,
                setNumber,
                weightKg,
                reps
        );

        return workoutSetRepository.save(workoutSet);
    }

    @Transactional
    public WorkoutSet updateSet(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            UUID setId,
            BigDecimal weightKg,
            Integer reps
    ) {
        Workout workout = workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        // Verify that the exercise belongs to this workout.
        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout exercise not found"));

        // Verify that the set belongs to this exercise.
        WorkoutSet workoutSet = workoutSetRepository
                .findByIdAndWorkoutExercise_Id(setId, workoutExerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout set not found"));

        workoutSet.setWeightKg(weightKg);
        workoutSet.setReps(reps);

        return workoutSetRepository.save(workoutSet);

    }

    @Transactional
    public WorkoutSet updateSetCompletion(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            UUID setId,
            boolean completed
    ) {
        Workout workout = workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout exercise not found"));

        WorkoutSet workoutSet = workoutSetRepository
                .findByIdAndWorkoutExercise_Id(setId, workoutExerciseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout set not found"));

        if (completed) {
            workoutSet.markCompleted();
        } else {
            workoutSet.markIncomplete();
        }

        return workoutSetRepository.save(workoutSet);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSet> getWorkoutSets(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId
    ) {
        // Verify workout ownership.
        workoutRepository.findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        // Verify that the exercise belongs to this workout.
        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout exercise not found"));

        return workoutSetRepository
                .findByWorkoutExercise_IdOrderBySetNumberAsc(workoutExerciseId);
    }

    @Transactional
    public Workout finishWorkout(UUID userId, UUID workoutId) {

        Workout workout = workoutRepository
                .findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException("Workout is already completed");
        }

        List<WorkoutExercise> exercises =
                workoutExerciseRepository
                        .findByWorkout_IdOrderByPositionAsc(workoutId);

        BigDecimal totalVolume = BigDecimal.ZERO;
        int completedSets = 0;

        for (WorkoutExercise exercise : exercises) {

            List<WorkoutSet> sets =
                    workoutSetRepository
                            .findByWorkoutExercise_IdOrderBySetNumberAsc(
                                    exercise.getId()
                            );

            for (WorkoutSet set : sets) {

                if (!set.isCompleted()) {
                    continue;
                }

                BigDecimal setVolume = set.getWeightKg()
                        .multiply(BigDecimal.valueOf(set.getReps()));

                totalVolume = totalVolume.add(setVolume);
                completedSets++;
            }
        }

        // PR detection will be implemented separately.
        int prCount = 0;

        workout.finish(totalVolume, completedSets, prCount);

        return workoutRepository.save(workout);
    }
}