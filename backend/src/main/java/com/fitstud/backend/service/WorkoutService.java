package com.fitstud.backend.service;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.entity.WorkoutSet;
import com.fitstud.backend.enums.SetType;
import com.fitstud.backend.enums.Visibility;
import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.WorkoutExerciseRepository;
import com.fitstud.backend.repository.WorkoutRepository;
import com.fitstud.backend.repository.WorkoutSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            WorkoutSetRepository workoutSetRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // 1. Start or create a workout
    @Transactional
    public Workout startWorkout(Workout workout) {
        workout.setStatus(WorkoutStatus.IN_PROGRESS);
        if (workout.getStartedAt() == null) {
            workout.setStartedAt(LocalDateTime.now());
        }
        return workoutRepository.save(workout);
    }

    // 2. Get workout by ID
    @Transactional(readOnly = true)
    public Workout getWorkoutById(UUID workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found with ID: " + workoutId));
    }

    // 3. Get all workouts for a user
    @Transactional(readOnly = true)
    public List<Workout> getUserWorkouts(UUID userId) {
        return workoutRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    // 4. Get public social feed workouts
    @Transactional(readOnly = true)
    public List<Workout> getPublicFeed() {
        return workoutRepository.findByStatusAndVisibilityOrderByCompletedAtDesc(
                WorkoutStatus.COMPLETED,
                Visibility.PUBLIC
        );
    }

    // 5. Add an Exercise to an active Workout
    @Transactional
    public WorkoutExercise addExerciseToWorkout(
            UUID workoutId,
            UUID exerciseId,
            Integer restTimerSeconds,
            Integer supersetGroupId,
            String notes
    ) {
        Workout workout = getWorkoutById(workoutId);
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with ID: " + exerciseId));

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setOrderIndex(workout.getExercises().size() + 1);
        workoutExercise.setRestTimerSeconds(restTimerSeconds != null ? restTimerSeconds : 90);
        workoutExercise.setSupersetGroupId(supersetGroupId);
        workoutExercise.setNotes(notes);

        workout.getExercises().add(workoutExercise);
        return workoutExerciseRepository.save(workoutExercise);
    }

    // 6. Add / Log a Set to a WorkoutExercise
    @Transactional
    public WorkoutSet addSetToExercise(UUID workoutExerciseId, WorkoutSet incomingSet) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new IllegalArgumentException("WorkoutExercise not found: " + workoutExerciseId));

        incomingSet.setWorkoutExercise(workoutExercise);
        if (incomingSet.getSetOrder() <= 0) {
            incomingSet.setSetOrder(workoutExercise.getSets().size() + 1);
        }
        if (incomingSet.getSetType() == null) {
            incomingSet.setSetType(SetType.NORMAL);
        }

        workoutExercise.getSets().add(incomingSet);
        return workoutSetRepository.save(incomingSet);
    }

    // 7. Update an existing Set (e.g., user changes weight/reps or ticks the completion checkbox)
    @Transactional
    public WorkoutSet updateSet(UUID setId, WorkoutSet updatedData) {
        WorkoutSet existingSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("WorkoutSet not found: " + setId));

        if (updatedData.getSetType() != null) existingSet.setSetType(updatedData.getSetType());
        if (updatedData.getWeightKg() != null) existingSet.setWeightKg(updatedData.getWeightKg());
        if (updatedData.getReps() != null) existingSet.setReps(updatedData.getReps());
        if (updatedData.getDurationSeconds() != null) existingSet.setDurationSeconds(updatedData.getDurationSeconds());
        if (updatedData.getDistanceKm() != null) existingSet.setDistanceKm(updatedData.getDistanceKm());
        if (updatedData.getRpe() != null) existingSet.setRpe(updatedData.getRpe());

        existingSet.setCompleted(updatedData.isCompleted());
        existingSet.setPersonalRecord(updatedData.isPersonalRecord());

        return workoutSetRepository.save(existingSet);
    }

    // 8. Complete Workout & Compute Denormalized Feed Metrics
    @Transactional
    public Workout completeWorkout(UUID workoutId) {
        Workout workout = getWorkoutById(workoutId);

        double totalVolume = 0.0;
        int completedSetsCount = 0;
        int prCount = 0;

        for (WorkoutExercise we : workout.getExercises()) {
            for (WorkoutSet set : we.getSets()) {
                if (set.isCompleted()) {
                    completedSetsCount++;

                    // Exclude warm-up sets from total working volume (Hevy standard)
                    if (set.getSetType() != SetType.WARMUP && set.getWeightKg() != null && set.getReps() != null) {
                        totalVolume += (set.getWeightKg() * set.getReps());
                    }

                    if (set.isPersonalRecord()) {
                        prCount++;
                    }
                }
            }
        }

        LocalDateTime finishedAt = LocalDateTime.now();
        workout.setStatus(WorkoutStatus.COMPLETED);
        workout.setCompletedAt(finishedAt);

        if (workout.getDurationSeconds() == null || workout.getDurationSeconds() == 0L) {
            long seconds = Math.max(0L, Duration.between(workout.getStartedAt(), finishedAt).getSeconds());
            workout.setDurationSeconds(seconds);
        }

        workout.setTotalVolumeKg(totalVolume);
        workout.setTotalSets(completedSetsCount);
        workout.setPrCount(prCount);

        return workoutRepository.save(workout);
    }

    // 9. Discard / Cancel a Workout
    @Transactional
    public Workout discardWorkout(UUID workoutId) {
        Workout workout = getWorkoutById(workoutId);
        workout.setStatus(WorkoutStatus.DISCARDED);
        return workoutRepository.save(workout);
    }
}