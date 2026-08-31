import { request } from './client';
import { CreateRidePayload, Ride } from '../types';

export const rideApi = {
  getAllRides: async (): Promise<Ride[]> => {
    return request<Ride[]>('/rides', {
      method: 'GET',
    });
  },

  getRideById: async (id: string): Promise<Ride> => {
    return request<Ride>(`/rides/${id}`, {
      method: 'GET',
    });
  },

  createRide: async (payload: CreateRidePayload): Promise<Ride[]> => {
    return request<Ride[]>('/rides', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};

