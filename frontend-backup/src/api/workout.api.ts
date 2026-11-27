
import api from './client';
import {
  WorkoutResponse,
  WorkoutDetailsResponse,
  WorkoutSetResponse,
  WorkoutExerciseResponse,
} from '@/types/workout';

export async function getWorkoutDetails(
  workoutId: string
): Promise<WorkoutDetailsResponse> {
  const response = await api.get<WorkoutDetailsResponse>(
    `/workouts/${workoutId}`
  );
  return response.data;
}

export async function addWorkoutExercise(
  workoutId: string,
  catalogExerciseId: string,
  position: number
): Promise<WorkoutExerciseResponse> {
  const response = await api.post<WorkoutExerciseResponse>(
    `/workouts/${workoutId}/exercises`,
    { exerciseId: catalogExerciseId, position, notes: null }
  );
  return response.data;
}

export async function addWorkoutSet(
  workoutId: string,
  workoutExerciseId: string
): Promise<WorkoutSetResponse> {
  const response = await api.post<WorkoutSetResponse>(
    `/workouts/${workoutId}/exercises/${workoutExerciseId}/sets`,
    { weightKg: null, reps: null }
  );
  return response.data;
}

export async function updateWorkoutSet(
  workoutId: string,
  workoutExerciseId: string,
  setId: string,
  weightKg: number | null,
  reps: number | null
): Promise<WorkoutSetResponse> {
  const response = await api.put<WorkoutSetResponse>(
    `/workouts/${workoutId}/exercises/${workoutExerciseId}/sets/${setId}`,
    { weightKg, reps }
  );
  return response.data;
}

export async function updateSetCompletion(
  workoutId: string,
  workoutExerciseId: string,
  setId: string,
  completed: boolean
): Promise<WorkoutSetResponse> {
  const response = await api.patch<WorkoutSetResponse>(
    `/workouts/${workoutId}/exercises/${workoutExerciseId}/sets/${setId}/completion`,
    { completed }
  );
  return response.data;
}

export async function deleteWorkoutSet(
  workoutId: string,
  workoutExerciseId: string,
  setId: string
): Promise<void> {
  await api.delete(
    `/workouts/${workoutId}/exercises/${workoutExerciseId}/sets/${setId}`
  );
}

export async function deleteWorkoutExercise(
  workoutId: string,
  workoutExerciseId: string
): Promise<void> {
  await api.delete(
    `/workouts/${workoutId}/exercises/${workoutExerciseId}`
  );
}

// This endpoint expects the catalog exercise ID, not the workout-exercise ID.
export async function getPreviousPerformance(
  workoutId: string,
  catalogExerciseId: string
): Promise<WorkoutSetResponse[]> {
  const response = await api.get<WorkoutSetResponse[]>(
    `/workouts/${workoutId}/exercises/${catalogExerciseId}/previous-performance`
  );
  return response.data;
}

export async function finishWorkout(
  workoutId: string
): Promise<WorkoutResponse> {
  const response = await api.post<WorkoutResponse>(
    `/workouts/${workoutId}/finish`
  );
  return response.data;
}

export async function startBlankWorkout(): Promise<WorkoutResponse> {
  const response = await api.post<WorkoutResponse>('/workouts');
  return response.data;
}

export async function startWorkoutFromRoutine(
  routineId: string
): Promise<WorkoutResponse> {
  const response = await api.post<WorkoutResponse>(
    `/workouts/from-routine/${routineId}`
  );
  return response.data;
}

export async function getWorkoutHistory(): Promise<WorkoutResponse[]> {
  const response = await api.get<WorkoutResponse[]>('/workouts');
  return response.data;
}

export async function getOngoingWorkouts(): Promise<WorkoutResponse[]> {
  const response = await api.get<WorkoutResponse[]>('/workouts/ongoing');
  return response.data;
}
