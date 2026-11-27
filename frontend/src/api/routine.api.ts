
import api from './client';
import {
  RoutineResponse,
  RoutineDetailsResponse,
  RoutineExerciseResponse,
  CreateRoutineRequest,
  UpdateRoutineRequest,
  AddRoutineExerciseRequest,
} from '../types/routine';

export async function getMyRoutines(): Promise<RoutineResponse[]> {
  const response = await api.get<RoutineResponse[]>('/routines');
  return response.data;
}

export async function getRoutineDetails(
  routineId: string,
): Promise<RoutineDetailsResponse> {
  const response = await api.get<RoutineDetailsResponse>(
    `/routines/${routineId}`,
  );
  return response.data;
}

export async function createRoutine(
  request: CreateRoutineRequest,
): Promise<RoutineResponse> {
  const response = await api.post<RoutineResponse>('/routines', request);
  return response.data;
}

export async function updateRoutine(
  routineId: string,
  request: UpdateRoutineRequest,
): Promise<RoutineResponse> {
  const response = await api.put<RoutineResponse>(
    `/routines/${routineId}`,
    request,
  );
  return response.data;
}

export async function deleteRoutine(routineId: string): Promise<void> {
  await api.delete(`/routines/${routineId}`);
}

export async function addRoutineExercise(
  routineId: string,
  request: AddRoutineExerciseRequest,
): Promise<RoutineExerciseResponse> {
  const response = await api.post<RoutineExerciseResponse>(
    `/routines/${routineId}/exercises`,
    request,
  );
  return response.data;
}

export async function removeRoutineExercise(
  routineId: string,
  exerciseId: string,
): Promise<void> {
  await api.delete(`/routines/${routineId}/exercises/${exerciseId}`);
}

export async function reorderRoutineExercise(
  routineId: string,
  exerciseId: string,
  newPosition: number,
): Promise<void> {
  await api.put(
    `/routines/${routineId}/exercises/${exerciseId}/position`,
    { newPosition },
  );
}
