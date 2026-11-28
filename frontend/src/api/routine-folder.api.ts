import api from './client';
import {
  RoutineFolderResponse,
  CreateRoutineFolderRequest,
  UpdateRoutineFolderRequest,
  MoveRoutineRequest,
} from '@/types/routine-folder';

export async function getRoutineFolders(): Promise<RoutineFolderResponse[]> {
  const response = await api.get<RoutineFolderResponse[]>('/routine-folders');
  return response.data;
}

export async function createRoutineFolder(
  data: CreateRoutineFolderRequest
): Promise<RoutineFolderResponse> {
  const response = await api.post<RoutineFolderResponse>(
    '/routine-folders',
    data
  );
  return response.data;
}

export async function renameRoutineFolder(
  folderId: string,
  data: UpdateRoutineFolderRequest
): Promise<RoutineFolderResponse> {
  const response = await api.put<RoutineFolderResponse>(
    `/routine-folders/${folderId}`,
    data
  );
  return response.data;
}

export async function deleteRoutineFolder(folderId: string): Promise<void> {
  await api.delete(`/routine-folders/${folderId}`);
}

export async function moveRoutineToFolder(
  routineId: string,
  data: MoveRoutineRequest
): Promise<void> {
  await api.put(`/routine-folders/routines/${routineId}`, data);
}

export async function removeRoutineFromFolder(
  routineId: string
): Promise<void> {
  await api.delete(`/routine-folders/routines/${routineId}`);
}