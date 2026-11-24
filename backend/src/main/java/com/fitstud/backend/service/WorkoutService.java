package com.fitstud.backend.service;

import com.fitstud.backend.dto.workout.*;
import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.exception.ResourceNotFoundException;
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
import com.fitstud.backend.mapper.WorkoutMapper;

import java.util.List;
import java.util.Optional;
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
    private final WorkoutMapper workoutMapper;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            UserRepository userRepository,
            RoutineRepository routineRepository,
            RoutineExerciseRepository routineExerciseRepository,
            ExerciseRepository exerciseRepository, WorkoutSetRepository workoutSetRepository,
            WorkoutMapper workoutMapper
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.userRepository = userRepository;
        this.routineRepository = routineRepository;
        this.routineExerciseRepository = routineExerciseRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.workoutMapper = workoutMapper;
    }

    @Transactional
    public WorkoutResponse startBlankWorkout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Workout workout = Workout.start(user, null);

        workout = workoutRepository.save(workout);
        return  workoutMapper.toWorkoutResponse(workout);
    }

    @Transactional
    public WorkoutResponse startWorkoutFromRoutine(
            UUID userId,
            UUID routineId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Routine routine = routineRepository
                .findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Routine not found"));

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

        return workoutMapper.toWorkoutResponse(workout);
    }

    @Transactional
    public WorkoutSetResponse addSet(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            BigDecimal weightKg,
            Integer reps
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        WorkoutExercise workoutExercise = workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        int setNumber = workoutSetRepository
                .findTopByWorkoutExercise_IdOrderBySetNumberDesc(workoutExerciseId)
                .map(set -> set.getSetNumber() + 1)
                .orElse(1);

        WorkoutSet workoutSet = WorkoutSet.create(
                workoutExercise,
                setNumber,
                weightKg,
                reps
        );

        workoutSet =  workoutSetRepository.save(workoutSet);
        return workoutMapper.toWorkoutSetResponse(workoutSet);
    }

    @Transactional
    public WorkoutSetResponse updateSet(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            UUID setId,
            BigDecimal weightKg,
            Integer reps
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        // Verify that the exercise belongs to this workout.
        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        // Verify that the set belongs to this exercise.
        WorkoutSet workoutSet = workoutSetRepository
                .findByIdAndWorkoutExercise_Id(setId, workoutExerciseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout set not found"));

        workoutSet.setWeightKg(weightKg);
        workoutSet.setReps(reps);

        workoutSet =  workoutSetRepository.save(workoutSet);

        return workoutMapper.toWorkoutSetResponse(workoutSet);
    }

    @Transactional
    public WorkoutSetResponse  updateSetCompletion(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            UUID setId,
            boolean completed
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        WorkoutSet workoutSet = workoutSetRepository
                .findByIdAndWorkoutExercise_Id(setId, workoutExerciseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout set not found"));

        if (completed) {
            workoutSet.markCompleted();
        } else {
            workoutSet.markIncomplete();
        }

        workoutSet =  workoutSetRepository.save(workoutSet);
        return workoutMapper.toWorkoutSetResponse(workoutSet);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetResponse> getWorkoutSets(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId
    ) {
        // Verify workout ownership.
        workoutRepository.findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        // Verify that the exercise belongs to this workout.
        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        return workoutSetRepository
                .findByWorkoutExercise_IdOrderBySetNumberAsc(workoutExerciseId)
                .stream()
                .map(workoutMapper::toWorkoutSetResponse)
                .toList();
    }

    @Transactional
    public WorkoutResponse  finishWorkout(UUID userId, UUID workoutId) {

        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

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

                if (set.getWeightKg() == null || set.getReps() == null) {
                    throw new IllegalStateException(
                            "Completed set must have both weight and reps"
                    );
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

        return workoutMapper.toWorkoutResponse(
                workoutRepository.save(workout)
        );
    }

    @Transactional(readOnly = true)
    public List<WorkoutSetResponse> getPreviousPerformance(
            UUID userId,
            UUID currentWorkoutId,
            UUID exerciseId
    ) {
        // Verify that the current workout belongs to the user.
        workoutRepository.findByIdAndUser_Id(currentWorkoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (!workoutExerciseRepository.existsByWorkout_IdAndExercise_Id(
                currentWorkoutId, exerciseId)) {
            throw new ResourceNotFoundException(
                    "Exercise does not belong to the current workout"
            );
        }
        // Find the most recent completed workout containing this exercise.
        Optional<WorkoutExercise> previousExercise =
                workoutExerciseRepository
                        .findFirstByWorkout_User_IdAndExercise_IdAndWorkout_StatusAndWorkout_IdNotOrderByWorkout_FinishedAtDesc(
                                userId,
                                exerciseId,
                                WorkoutStatus.COMPLETED,
                                currentWorkoutId
                        );

        if (previousExercise.isEmpty()) {
            return List.of();
        }

        // Return its sets in set-number order.
        return workoutSetRepository
                .findByWorkoutExercise_IdOrderBySetNumberAsc(
                        previousExercise.get().getId()
                )
                .stream()
                .map(workoutMapper::toWorkoutSetResponse)
                .toList();
    }

    @Transactional
    public void deleteSet(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            UUID setId
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        WorkoutSet set = workoutSetRepository
                .findByIdAndWorkoutExercise_Id(setId, workoutExerciseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout set not found"));

        workoutSetRepository.delete(set);
        workoutSetRepository.flush();

        List<WorkoutSet> remainingSets =
                workoutSetRepository
                        .findByWorkoutExercise_IdOrderBySetNumberAsc(
                                workoutExerciseId
                        );

        // First move all sets to temporary unique numbers.
        int maxSetNumber = remainingSets.isEmpty()
                ? 0
                : remainingSets.get(remainingSets.size() - 1).getSetNumber();

        int offset = Math.addExact(
                maxSetNumber,
                remainingSets.size() + 1
        );

        for (WorkoutSet remainingSet : remainingSets) {
            remainingSet.setSetNumber(
                    remainingSet.getSetNumber() + offset
            );
        }

        workoutSetRepository.saveAllAndFlush(remainingSets);

        // Now assign consecutive set numbers starting from 1.
        for (int i = 0; i < remainingSets.size(); i++) {
            remainingSets.get(i).setSetNumber(i + 1);
        }

        workoutSetRepository.saveAll(remainingSets);
    }

    @Transactional
    public void deleteExercise(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }

        WorkoutExercise workoutExercise = workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, workoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        // Retrieve all sets belonging to this exercise.
        List<WorkoutSet> sets =
                workoutSetRepository
                        .findByWorkoutExercise_IdOrderBySetNumberAsc(
                                workoutExerciseId
                        );

        // Delete the sets first to satisfy the foreign key constraint.
        workoutSetRepository.deleteAll(sets);

        // Delete the exercise.
        workoutExerciseRepository.delete(workoutExercise);
        workoutExerciseRepository.flush();

        List<WorkoutExercise> remainingExercises =
                workoutExerciseRepository
                        .findByWorkout_IdOrderByPositionAsc(workoutId);

        if (!remainingExercises.isEmpty()) {
            int offset = remainingExercises.stream()
                    .mapToInt(WorkoutExercise::getPosition)
                    .max()
                    .orElse(0) + 1;

            // Move all positions temporarily to avoid unique constraint conflicts.
            for (WorkoutExercise exercise : remainingExercises) {
                exercise.setPosition(exercise.getPosition() + offset);
            }


            // Assign contiguous positions starting from zero.
            for (int i = 0; i < remainingExercises.size(); i++) {
                remainingExercises.get(i).setPosition(i);
            }
        }
    }

    @Transactional
    public void reorderExercise(
            UUID userId,
            UUID workoutId,
            UUID workoutExerciseId,
            int newPosition
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

        if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot modify a completed workout"
            );
        }



        List<WorkoutExercise> exercises =
                workoutExerciseRepository
                        .findByWorkout_IdOrderByPositionAsc(workoutId);

        if (newPosition < 0 || newPosition >= exercises.size()) {
            throw new IllegalArgumentException("Invalid position");
        }

        WorkoutExercise target = exercises.stream()
                .filter(e -> e.getId().equals(workoutExerciseId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout exercise not found"));

        exercises.remove(target);
        exercises.add(newPosition, target);

        // Move all exercises to temporary positions first.
        int maxPosition = workoutExerciseRepository
                .findTopByWorkout_IdOrderByPositionDesc(workoutId)
                .map(WorkoutExercise::getPosition)
                .orElse(-1);

        int offset = Math.addExact(
                maxPosition,
                exercises.size() + 1
        );

        for (WorkoutExercise exercise : exercises) {
            exercise.setPosition(exercise.getPosition() + offset);
        }

        workoutExerciseRepository.saveAllAndFlush(exercises);

        // Assign final consecutive positions.
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setPosition(i);
        }

        workoutExerciseRepository.saveAll(exercises);
    }

    @Transactional
    public WorkoutExerciseResponse addExercise(
            UUID userId,
            UUID workoutId,
            UUID exerciseId,
            int position,
            String notes
    ) {
        Workout workout = workoutRepository
                .findByIdAndUserIdForUpdate(workoutId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found"));

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
                        new ResourceNotFoundException("Exercise not found"));

        boolean isGlobal = exercise.getOwner() == null;

        boolean isOwnedByUser = exercise.getOwner() != null
                && exercise.getOwner().getId().equals(userId);

        if (!isGlobal && !isOwnedByUser) {
            throw new ResourceNotFoundException("Exercise not found");
        }

        if (workoutExerciseRepository
                .existsByWorkout_IdAndExercise_Id(workoutId, exerciseId)) {
            throw new IllegalStateException(
                    "Exercise already exists in this workout"
            );
        }

        long exerciseCount =
                workoutExerciseRepository.countByWorkout_Id(workoutId);

        if (position != exerciseCount) {
            throw new IllegalArgumentException(
                    "New exercise must be added at the next available position"
            );
        }

        WorkoutExercise workoutExercise = WorkoutExercise.create(
                workout,
                exercise,
                position,
                notes
        );

        workoutExercise =  workoutExerciseRepository.save(workoutExercise);
        return workoutMapper.toWorkoutExerciseResponse(workoutExercise);
    }

    @Transactional(readOnly = true)
    public WorkoutDetailsResponse getWorkoutDetails(UUID userId, UUID workoutId) {

        Workout workout = workoutRepository.findByIdAndUser_Id(workoutId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        List<WorkoutExercise> exercises =
                workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId);

        List<WorkoutExerciseDetailsResponse> exerciseDetails = exercises.stream()
                .map(exercise -> {
                    List<WorkoutSet> sets =
                            workoutSetRepository.findByWorkoutExercise_IdOrderBySetNumberAsc(
                                    exercise.getId()
                            );

                    return workoutMapper.toWorkoutExerciseDetailsResponse(exercise, sets);
                })
                .toList();

        return workoutMapper.toWorkoutDetailsResponse(workout, exerciseDetails);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> getWorkoutHistoryResponses(UUID userId) {

        return workoutRepository.findByUser_IdAndStatusOrderByStartedAtDesc(
                        userId,
                        WorkoutStatus.COMPLETED
                )
                .stream()
                .map(workoutMapper::toWorkoutResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> getOngoingWorkoutResponses(UUID userId) {

        return workoutRepository.findByUser_IdAndStatusOrderByStartedAtDesc(
                        userId,
                        WorkoutStatus.IN_PROGRESS
                )
                .stream()
                .map(workoutMapper::toWorkoutResponse)
                .toList();
    }
}