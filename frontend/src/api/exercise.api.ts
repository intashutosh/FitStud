import api from './client';
import { ExerciseResponse } from '@/types/exercise';

export async function getExerciseCatalog(): Promise<ExerciseResponse[]> {
  const response = await api.get<ExerciseResponse[]>('/exercises');
  return response.data;
}