export interface RoutineFolderResponse {
  id: string;
  name: string;
  position: number;
  routineCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRoutineFolderRequest {
  name: string;
}

export interface UpdateRoutineFolderRequest {
  name: string;
}

export interface MoveRoutineRequest {
  folderId: string;
}