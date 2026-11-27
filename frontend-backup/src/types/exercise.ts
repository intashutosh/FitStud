export interface ExerciseResponse {
  id: string;
  name: string;
  description: string | null;
  muscleGroup: string | null;
  equipment: string | null;
  global: boolean;
  createdAt: string;
  updatedAt: string;
}