export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  displayName: string;
}

export interface RegisterResponse {
  id: string;
  email: string;
  username: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  username: string;
  displayName: string;
}

export interface UserProfile {
  id: string;
  email: string;
  username: string;
  displayName: string;
  bio: string | null;
  profilePhotoUrl: string | null;
  createdAt: string;
}