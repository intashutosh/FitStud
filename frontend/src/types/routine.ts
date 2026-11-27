
export interface RoutineResponse {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RoutineExerciseResponse {
  id: string;
  exerciseId: string;
  exerciseName: string;
  position: number;
  notes: string | null;
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
