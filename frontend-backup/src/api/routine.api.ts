import api from './client';
import { RoutineResponse } from '@/types/routine';

export async function getMyRoutines(): Promise<RoutineResponse[]> {
  const response = await api.get<RoutineResponse[]>('/routines');
  return response.data;
}