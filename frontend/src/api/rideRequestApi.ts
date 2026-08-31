import { request } from './client';
import { CreateRideRequestPayload, RideRequest } from '../types';

export const rideRequestApi = {
  getAllRideRequests: async (rideId?: string, passengerId?: string): Promise<RideRequest[]> => {
    const params = new URLSearchParams();
    if (rideId) params.append('rideId', rideId);
    if (passengerId) params.append('passengerId', passengerId);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return request<RideRequest[]>(`/ride-requests${queryString}`, {
      method: 'GET',
    });
  },

  getRideRequestById: async (id: string): Promise<RideRequest> => {
    return request<RideRequest>(`/ride-requests/${id}`, {
      method: 'GET',
    });
  },

  createRideRequest: async (payload: CreateRideRequestPayload): Promise<RideRequest> => {
    return request<RideRequest>('/ride-requests', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  updateRideRequestStatus: async (id: string, status: string): Promise<RideRequest> => {
    return request<RideRequest>(`/ride-requests/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  },
};

