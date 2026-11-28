
export interface RoutineResponse {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  folderId: string | null;

}

export interface RoutineExerciseResponse {
  id: string;
  exerciseId: string;
  exerciseName: string;
  position: number;
  notes: string | null;
  folderId: string | null;
}

export interface RoutineDetailsResponse {
  routine: RoutineResponse;
  exercises: RoutineExerciseResponse[];
}

export interface CreateRoutineRequest {
  name: string;
  description?: string;
}

export interface UpdateRoutineRequest {
  name: string;
  description?: string;
}

export interface AddRoutineExerciseRequest {
  exerciseId: string;
  position: number;
  notes?: string;
}

export interface ReorderRoutineExerciseRequest {
  newPosition: number;
}

