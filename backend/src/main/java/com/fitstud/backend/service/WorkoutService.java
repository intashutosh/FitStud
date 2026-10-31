package com.fitstud.backend.service;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.entity.Workout;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.repository.ExerciseRepository;
import com.fitstud.backend.repository.RoutineExerciseRepository;
import com.fitstud.backend.repository.RoutineRepository;
import com.fitstud.backend.repository.UserRepository;
import com.fitstud.backend.repository.WorkoutExerciseRepository;
import com.fitstud.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public WorkoutService(
            WorkoutRepository workoutRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            UserRepository userRepository,
            RoutineRepository routineRepository,
            RoutineExerciseRepository routineExerciseRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.userRepository = userRepository;
        this.routineRepository = routineRepository;
        this.routineExerciseRepository = routineExerciseRepository;
        this.exerciseRepository = exerciseRepository;
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
}