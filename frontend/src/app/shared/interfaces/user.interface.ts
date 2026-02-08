export interface User {
  id?: number;
  createdAt?: string;
  updatedAt?: string;
  deleted?: boolean;
  email: string;
  fullName: string;
  password: string;
  role?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}
