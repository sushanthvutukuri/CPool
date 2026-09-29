export interface User {
  id: string;
  name: string;
  email: string;
  phoneNumber?: string;
}

export interface Ride {
  id: string;
  driverId: string;
  driverPhone?: string;
  origin: string;
  destination: string;
  departureTime: string; // ISO-8601 string, e.g. "2026-01-18T20:00:00"
  availableSeats: number;
  status: string;
  createdAt: string;
  recurring: boolean;
}

export interface RideRequest {
  id: string;
  rideId: string;
  passengerId: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';
  createdAt: string;
}

export interface CreateRidePayload {
  driverId: string;
  origin: string;
  destination: string;
  departureTime: string;
  availableSeats: number;
  recurring: boolean;
}

export interface CreateRideRequestPayload {
  rideId: string;
  passengerId: string;
}

export interface AuthResponse {
  status: string;
  userId?: string;
  name?: string;
  email?: string;
  phoneNumber?: string;
}

export interface ApiError {
  status?: number;
  error?: string;
  message: string;
  errors?: Record<string, string>;
}
