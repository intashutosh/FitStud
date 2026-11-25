export type WorkoutStatus = 'IN_PROGRESS' | 'COMPLETED';

export type SessionMode =
  | 'STANDARD'
  | 'TIME_COMPRESSED'
  | 'LOW_STRESS_MAINTENANCE';

export type WorkoutVisibility =
  | 'PUBLIC'
  | 'ORGANIZATION_ONLY'
  | 'FOLLOWERS_ONLY'
  | 'PRIVATE';

export interface WorkoutResponse {
  id: string;
  sourceRoutineId: string | null;
  startedAt: string;
  finishedAt: string | null;
  status: WorkoutStatus;
  sessionMode: SessionMode;
  visibility: WorkoutVisibility;
  durationSeconds: number | null;
  totalVolumeKg: number;
  totalSets: number;
  prCount: number;
}

export interface WorkoutExerciseResponse {
  id: string;
  exerciseId: string;
  exerciseName: string;
  position: number;
  notes: string | null;
}

export interface WorkoutSetResponse {
  id: string;
  setNumber: number;
  weightKg: number | null;
  reps: number | null;
  completed: boolean;
  completedAt: string | null;
}

export interface WorkoutExerciseDetailsResponse {
  exercise: WorkoutExerciseResponse;
  sets: WorkoutSetResponse[];
}

export interface WorkoutDetailsResponse {
  workout: WorkoutResponse;
  exercises: WorkoutExerciseDetailsResponse[];
}