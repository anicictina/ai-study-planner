import { PreferredTime } from './profile.model';

export type UserRole = 'STUDENT' | 'ADMIN';

export interface AuthUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  preferredStudyTime: PreferredTime | null;
}

export interface AuthResponse extends AuthUser {
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}
