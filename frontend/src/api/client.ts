import { ApiError } from '../types';

const BASE_URL = '/api';

export async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (typeof data === 'object' && data !== null) {
      const err = data as ApiError;
      throw new Error(err.message || err.error || `HTTP error ${response.status}`);
    }
    throw new Error(data || `HTTP error ${response.status}`);
  }

  return data as T;
}

