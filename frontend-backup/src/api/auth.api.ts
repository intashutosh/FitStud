import api from './client';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserProfile,
} from '@/types/auth';

export async function registerUser(
  data: RegisterRequest
): Promise<RegisterResponse> {
  const response = await api.post<RegisterResponse>(
    '/auth/register',
    data
  );

  return response.data;
}

export async function loginUser(
  data: LoginRequest
): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>(
    '/auth/login',
    data
  );

  return response.data;
}

export async function getCurrentUser(): Promise<UserProfile> {
  const response = await api.get<UserProfile>('/users/me');
  return response.data;
}