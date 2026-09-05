export interface AuthUser {
  id: number;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'APPRENANT';
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}
