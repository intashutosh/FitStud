
package com.fitstud.backend.service;

import com.fitstud.backend.dto.workout.WorkoutResponse;
import com.fitstud.backend.dto.workout.WorkoutSetResponse;
import com.fitstud.backend.entity.*;
import com.fitstud.backend.enums.WorkoutStatus;
import com.fitstud.backend.exception.ResourceNotFoundException;
import com.fitstud.backend.mapper.WorkoutMapper;
import com.fitstud.backend.repository.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineExercise;
import com.fitstud.backend.entity.WorkoutExercise;
import com.fitstud.backend.enums.SessionMode;
import com.fitstud.backend.enums.WorkoutVisibility;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private RoutineExerciseRepository routineExerciseRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutSetRepository workoutSetRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @InjectMocks
    private WorkoutService workoutService;

    // 1. Successfully start a blank workout.
    @Test
    void startBlankWorkout_shouldCreateWorkout() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        WorkoutResponse expectedResponse = mock(WorkoutResponse.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(workoutMapper.toWorkoutResponse(any(Workout.class)))
                .thenReturn(expectedResponse);

        WorkoutResponse result =
                workoutService.startBlankWorkout(userId);

        assertSame(expectedResponse, result);

        verify(workoutRepository).save(argThat(workout ->
                workout.getStatus() == WorkoutStatus.IN_PROGRESS
                        && workout.getSourceRoutine() == null
        ));
    }

    // 2. Starting a workout should fail if the user does not exist.
    @Test
    void startBlankWorkout_shouldThrowWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.startBlankWorkout(userId)
        );

        verify(workoutRepository, never()).save(any());
    }

    // 3. Adding a set should assign the next available set number.
    @Test
    void addSet_shouldAssignNextSetNumber() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID workoutExerciseId = UUID.randomUUID();

        User user = mock(User.class);
        Workout workout = Workout.start(user, null);

        WorkoutExercise exercise = mock(WorkoutExercise.class);

        when(workoutRepository.findByIdAndUserIdForUpdate(
                workoutId, userId
        )).thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByIdAndWorkout_Id(
                workoutExerciseId, workoutId
        )).thenReturn(Optional.of(exercise));

        when(workoutSetRepository
                .findTopByWorkoutExercise_IdOrderBySetNumberDesc(
                        workoutExerciseId
                )).thenReturn(Optional.empty());

        when(workoutSetRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkoutSetResponse expectedResponse =
                mock(WorkoutSetResponse.class);

        when(workoutMapper.toWorkoutSetResponse(any()))
                .thenReturn(expectedResponse);

        WorkoutSetResponse result = workoutService.addSet(
                userId,
                workoutId,
                workoutExerciseId,
                new BigDecimal("20.00"),
                10
        );

        assertSame(expectedResponse, result);

        verify(workoutSetRepository).save(argThat(set ->
                set.getSetNumber() == 1
                        && set.getWeightKg().compareTo(
                        new BigDecimal("20.00")
                ) == 0
                        && set.getReps() == 10
                        && !set.isCompleted()
        ));
    }

    // 4. Adding a set should fail for a workout owned by another user.
    @Test
    void addSet_shouldRejectWorkoutNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID workoutExerciseId = UUID.randomUUID();

        when(workoutRepository.findByIdAndUserIdForUpdate(
                workoutId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.addSet(
                        userId,
                        workoutId,
                        workoutExerciseId,
                        new BigDecimal("20"),
                        10
                )
        );

        verify(workoutSetRepository, never()).save(any());
    }

    // 5. A completed workout must not accept new sets.
    @Test
    void addSet_shouldRejectCompletedWorkout() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID workoutExerciseId = UUID.randomUUID();

        User user = mock(User.class);
        Workout workout = Workout.start(user, null);

        // Finish the workout so its status changes.
        setWorkoutStartedAt(workout);
        workout.finish(BigDecimal.ZERO, 0, 0);

        when(workoutRepository.findByIdAndUserIdForUpdate(
                workoutId, userId
        )).thenReturn(Optional.of(workout));

        assertThrows(
                IllegalStateException.class,
                () -> workoutService.addSet(
                        userId,
                        workoutId,
                        workoutExerciseId,
                        new BigDecimal("20"),
                        10
                )
        );

        verify(workoutSetRepository, never()).save(any());
    }


    @Test
    void startWorkoutFromRoutine_shouldCopyRoutineExercises() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        User user = mock(User.class);
        Routine routine = mock(Routine.class);

        Exercise exercise1 = mock(Exercise.class);
        Exercise exercise2 = mock(Exercise.class);

        RoutineExercise routineExercise1 = mock(RoutineExercise.class);
        RoutineExercise routineExercise2 = mock(RoutineExercise.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(routineRepository.findByIdAndUser_Id(routineId, userId))
                .thenReturn(Optional.of(routine));

        when(routineExercise1.getExercise()).thenReturn(exercise1);
        when(routineExercise1.getPosition()).thenReturn(0);
        when(routineExercise1.getNotes()).thenReturn("First exercise");

        when(routineExercise2.getExercise()).thenReturn(exercise2);
        when(routineExercise2.getPosition()).thenReturn(1);
        when(routineExercise2.getNotes()).thenReturn("Second exercise");

        when(routineExerciseRepository.findByRoutine_IdOrderByPositionAsc(routineId))
                .thenReturn(List.of(routineExercise1, routineExercise2));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkoutResponse response = new WorkoutResponse(
                null, null, null, null,
                WorkoutStatus.IN_PROGRESS,
                SessionMode.STANDARD,
                WorkoutVisibility.PRIVATE,
                null, BigDecimal.ZERO, 0, 0
        );

        when(workoutMapper.toWorkoutResponse(any(Workout.class)))
                .thenReturn(response);

        WorkoutResponse result =
                workoutService.startWorkoutFromRoutine(userId, routineId);

        assertEquals(WorkoutStatus.IN_PROGRESS, result.status());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<WorkoutExercise>> captor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(workoutExerciseRepository).saveAll(captor.capture());

        List<WorkoutExercise> copiedExercises =
                StreamSupport.stream(captor.getValue().spliterator(), false).toList();

        assertEquals(2, copiedExercises.size());
        assertEquals(0, copiedExercises.get(0).getPosition());
        assertEquals(1, copiedExercises.get(1).getPosition());

        verify(workoutRepository).save(any(Workout.class));
    }

    @Test
    void startWorkoutFromRoutine_shouldRejectUnownedRoutine() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        User user = mock(User.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(routineRepository.findByIdAndUser_Id(routineId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.startWorkoutFromRoutine(userId, routineId)
        );

        verify(workoutRepository, never()).save(any(Workout.class));
        verify(workoutExerciseRepository, never()).saveAll(any());
    }

    @Test
    void finishWorkout_shouldCalculateVolumeUsingOnlyCompletedSets() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();

        User user = mock(User.class);
        Workout workout = Workout.start(user, null);

        setWorkoutStartedAt(workout);

        WorkoutExercise exercise = mock(WorkoutExercise.class);
        when(exercise.getId()).thenReturn(UUID.randomUUID());

        WorkoutSet set1 = mock(WorkoutSet.class);
        when(set1.isCompleted()).thenReturn(true);
        when(set1.getWeightKg()).thenReturn(new BigDecimal("20"));
        when(set1.getReps()).thenReturn(10);

        WorkoutSet set2 = mock(WorkoutSet.class);
        when(set2.isCompleted()).thenReturn(true);
        when(set2.getWeightKg()).thenReturn(new BigDecimal("30"));
        when(set2.getReps()).thenReturn(5);

        WorkoutSet set3 = mock(WorkoutSet.class);
        when(set3.isCompleted()).thenReturn(false);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(List.of(exercise));

        when(workoutSetRepository.findByWorkoutExercise_IdOrderBySetNumberAsc(
                exercise.getId()
        )).thenReturn(List.of(set1, set2, set3));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkoutResponse response = new WorkoutResponse(
                null, null, null, null,
                WorkoutStatus.COMPLETED,
                SessionMode.STANDARD,
                WorkoutVisibility.PRIVATE,
                60, new BigDecimal("350"), 2, 0
        );

        when(workoutMapper.toWorkoutResponse(any(Workout.class)))
                .thenReturn(response);

        WorkoutResponse result = workoutService.finishWorkout(userId, workoutId);

        assertEquals(WorkoutStatus.COMPLETED, result.status());
        assertEquals(new BigDecimal("350"), workout.getTotalVolumeKg());
        assertEquals(2, workout.getTotalSets());
        assertEquals(WorkoutStatus.COMPLETED, workout.getStatus());

        verify(workoutRepository).save(workout);
    }

    @Test
    void finishWorkout_shouldRejectCompletedSetWithMissingWeightOrReps() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();

        User user = mock(User.class);
        Workout workout = Workout.start(user, null);

        setWorkoutStartedAt(workout);

        WorkoutExercise exercise = mock(WorkoutExercise.class);
        when(exercise.getId()).thenReturn(UUID.randomUUID());

        WorkoutSet invalidSet = mock(WorkoutSet.class);
        when(invalidSet.isCompleted()).thenReturn(true);
        when(invalidSet.getWeightKg()).thenReturn(null);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(List.of(exercise));

        when(workoutSetRepository.findByWorkoutExercise_IdOrderBySetNumberAsc(
                exercise.getId()
        )).thenReturn(List.of(invalidSet));

        assertThrows(
                IllegalStateException.class,
                () -> workoutService.finishWorkout(userId, workoutId)
        );

        assertEquals(WorkoutStatus.IN_PROGRESS, workout.getStatus());

        verify(workoutRepository, never()).save(any(Workout.class));
    }

    private void setEntityId(Object entity, UUID id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Workout createInProgressWorkout() {
        User user = mock(User.class);
        Workout workout = Workout.start(user, null);
        setWorkoutStartedAt(workout);
        return workout;
    }

    @Test
    void updateSetCompletion_shouldMarkSetCompletedAndSetTimestamp() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();
        WorkoutExercise exercise = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );
        WorkoutSet set = WorkoutSet.create(
                exercise, 1, new BigDecimal("20"), 10
        );

        setEntityId(exercise, exerciseId);
        setEntityId(set, setId);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByIdAndWorkout_Id(exerciseId, workoutId))
                .thenReturn(Optional.of(exercise));
        when(workoutSetRepository.findByIdAndWorkoutExercise_Id(setId, exerciseId))
                .thenReturn(Optional.of(set));
        when(workoutSetRepository.save(any(WorkoutSet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        workoutService.updateSetCompletion(
                userId, workoutId, exerciseId, setId, true
        );

        assertTrue(set.isCompleted());
        assertNotNull(set.getCompletedAt());

        verify(workoutSetRepository).save(set);
    }

    @Test
    void updateSetCompletion_shouldClearTimestampWhenMarkedIncomplete() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();
        WorkoutExercise exercise = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );
        WorkoutSet set = WorkoutSet.create(
                exercise, 1, new BigDecimal("20"), 10
        );

        set.markCompleted();

        setEntityId(exercise, exerciseId);
        setEntityId(set, setId);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByIdAndWorkout_Id(exerciseId, workoutId))
                .thenReturn(Optional.of(exercise));
        when(workoutSetRepository.findByIdAndWorkoutExercise_Id(setId, exerciseId))
                .thenReturn(Optional.of(set));
        when(workoutSetRepository.save(any(WorkoutSet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        workoutService.updateSetCompletion(
                userId, workoutId, exerciseId, setId, false
        );

        assertFalse(set.isCompleted());
        assertNull(set.getCompletedAt());

        verify(workoutSetRepository).save(set);
    }

    @Test
    void deleteSet_shouldDeleteSetAndRenumberRemainingSets() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID deletedSetId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();
        WorkoutExercise exercise = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );

        WorkoutSet set1 = WorkoutSet.create(
                exercise, 1, new BigDecimal("20"), 10
        );
        WorkoutSet set2 = WorkoutSet.create(
                exercise, 2, new BigDecimal("25"), 10
        );
        WorkoutSet set3 = WorkoutSet.create(
                exercise, 3, new BigDecimal("30"), 8
        );

        setEntityId(exercise, exerciseId);
        setEntityId(set2, deletedSetId);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByIdAndWorkout_Id(exerciseId, workoutId))
                .thenReturn(Optional.of(exercise));
        when(workoutSetRepository.findByIdAndWorkoutExercise_Id(
                deletedSetId, exerciseId
        )).thenReturn(Optional.of(set2));
        when(workoutSetRepository.findByWorkoutExercise_IdOrderBySetNumberAsc(
                exerciseId
        )).thenReturn(List.of(set1, set3));

        workoutService.deleteSet(userId, workoutId, exerciseId, deletedSetId);

        assertEquals(1, set1.getSetNumber());
        assertEquals(2, set3.getSetNumber());

        verify(workoutSetRepository).delete(set2);
        verify(workoutSetRepository).flush();
        verify(workoutSetRepository).saveAllAndFlush(anyIterable());
        verify(workoutSetRepository).saveAll(anyIterable());
    }

    @Test
    void reorderExercise_shouldUpdateExercisePositions() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();

        WorkoutExercise first = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );
        WorkoutExercise second = WorkoutExercise.create(
                workout, mock(Exercise.class), 1, null
        );
        WorkoutExercise third = WorkoutExercise.create(
                workout, mock(Exercise.class), 2, null
        );

        setEntityId(first, UUID.randomUUID());
        setEntityId(second, UUID.randomUUID());
        setEntityId(third, exerciseId);

        List<WorkoutExercise> exercises = new ArrayList<>(
                List.of(first, second, third)
        );

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(exercises);

        workoutService.reorderExercise(
                userId, workoutId, exerciseId, 0
        );

        assertEquals(0, third.getPosition());
        assertEquals(1, first.getPosition());
        assertEquals(2, second.getPosition());

        verify(workoutExerciseRepository).saveAllAndFlush(anyIterable());
        verify(workoutExerciseRepository).saveAll(anyIterable());
    }

    private void setWorkoutStartedAt(Workout workout) {
        try {
            var field = Workout.class.getDeclaredField("startedAt");
            field.setAccessible(true);
            field.set(workout, java.time.Instant.now().minusSeconds(60));
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void deleteExercise_shouldDeleteItsSetsAndRenumberRemainingExercises() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID deletedExerciseId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();

        WorkoutExercise deletedExercise = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );
        WorkoutExercise remaining1 = WorkoutExercise.create(
                workout, mock(Exercise.class), 1, null
        );
        WorkoutExercise remaining2 = WorkoutExercise.create(
                workout, mock(Exercise.class), 2, null
        );

        setEntityId(deletedExercise, deletedExerciseId);
        setEntityId(remaining1, UUID.randomUUID());
        setEntityId(remaining2, UUID.randomUUID());

        WorkoutSet set1 = WorkoutSet.create(
                deletedExercise, 1, new BigDecimal("20"), 10
        );
        WorkoutSet set2 = WorkoutSet.create(
                deletedExercise, 2, new BigDecimal("25"), 8
        );

        List<WorkoutExercise> remainingExercises =
                new ArrayList<>(List.of(remaining1, remaining2));

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByIdAndWorkout_Id(
                deletedExerciseId, workoutId
        )).thenReturn(Optional.of(deletedExercise));

        when(workoutSetRepository.findByWorkoutExercise_IdOrderBySetNumberAsc(
                deletedExerciseId
        )).thenReturn(List.of(set1, set2));

        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(remainingExercises);

        workoutService.deleteExercise(userId, workoutId, deletedExerciseId);

        assertEquals(0, remaining1.getPosition());
        assertEquals(1, remaining2.getPosition());

        verify(workoutSetRepository).deleteAll(anyIterable());
        verify(workoutExerciseRepository).delete(deletedExercise);
        verify(workoutExerciseRepository).flush();
    }

    @Test
    void reorderExercise_shouldRejectInvalidPosition() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();

        WorkoutExercise first = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );
        WorkoutExercise second = WorkoutExercise.create(
                workout, mock(Exercise.class), 1, null
        );

        setEntityId(first, UUID.randomUUID());
        setEntityId(second, exerciseId);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(new ArrayList<>(List.of(first, second)));

        assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.reorderExercise(
                        userId, workoutId, exerciseId, 2
                )
        );

        assertEquals(0, first.getPosition());
        assertEquals(1, second.getPosition());

        verify(workoutExerciseRepository, never()).saveAllAndFlush(anyIterable());
    }

    @Test
    void reorderExercise_shouldRejectExerciseNotInWorkout() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID requestedExerciseId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();

        WorkoutExercise existing = WorkoutExercise.create(
                workout, mock(Exercise.class), 0, null
        );

        setEntityId(existing, UUID.randomUUID());

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findByWorkout_IdOrderByPositionAsc(workoutId))
                .thenReturn(new ArrayList<>(List.of(existing)));

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.reorderExercise(
                        userId, workoutId, requestedExerciseId, 0
                )
        );

        verify(workoutExerciseRepository, never()).saveAllAndFlush(anyIterable());
    }

    @Test
    void deleteExercise_shouldRejectCompletedWorkout() {
        UUID userId = UUID.randomUUID();
        UUID workoutId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        Workout workout = createInProgressWorkout();
        workout.finish(BigDecimal.ZERO, 0, 0);

        when(workoutRepository.findByIdAndUserIdForUpdate(workoutId, userId))
                .thenReturn(Optional.of(workout));

        assertThrows(
                IllegalStateException.class,
                () -> workoutService.deleteExercise(
                        userId, workoutId, exerciseId
                )
        );

        verify(workoutExerciseRepository, never()).delete(any());
        verify(workoutSetRepository, never()).deleteAll(anyIterable());
    }
}
