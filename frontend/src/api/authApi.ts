import { request } from './client';
import { AuthResponse, User } from '../types';

export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    return request<AuthResponse>('/auth/authenticate-user', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },

  signup: async (name: string, email: string, password: string, phoneNumber?: string): Promise<User> => {
    return request<User>('/users/create', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, phoneNumber: phoneNumber || '' }),
    });
  },

  getUserById: async (id: string): Promise<User> => {
    return request<User>(`/users/${id}`, {
      method: 'GET',
    });
  },
};
