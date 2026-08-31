import React, { useState } from 'react';
import { MapPin, Clock, Users, Repeat, CheckCircle2, AlertCircle } from 'lucide-react';
import { Ride } from '../types';
import { useAuth } from '../context/AuthContext';
import { rideRequestApi } from '../api/rideRequestApi';

interface RideCardProps {
  ride: Ride;
  onRequestSuccess?: () => void;
}

export const RideCard: React.FC<RideCardProps> = ({ ride, onRequestSuccess }) => {
  const { user } = useAuth();
  const [isRequesting, setIsRequesting] = useState(false);
  const [requestStatus, setRequestStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');

  const isDriver = user?.id === ride.driverId;

  // Format departure date and time
  const formatDeparture = (dateStr: string) => {
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
      });
    } catch {
      return dateStr;
    }
  };

  const handleRequestSeat = async () => {
    if (!user) return;
    setIsRequesting(true);
    setRequestStatus('idle');
    setErrorMessage('');

    try {
      await rideRequestApi.createRideRequest({
        rideId: ride.id,
        passengerId: user.id,
      });
      setRequestStatus('success');
      if (onRequestSuccess) onRequestSuccess();
    } catch (err: any) {
      setRequestStatus('error');
      setErrorMessage(err.message || 'Failed to request seat');
    } finally {
      setIsRequesting(false);
    }
  };

  return (
    <div className="group bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/80 dark:border-slate-800 p-5 sm:p-6 shadow-sm hover:shadow-md hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Route details */}
        <div className="flex-1 space-y-3">
          <div className="flex items-center space-x-2">
            {ride.recurring && (
              <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-50 text-teal-700 dark:bg-teal-950/60 dark:text-teal-300 border border-teal-200 dark:border-teal-800">
                <Repeat className="w-3 h-3" />
                <span>Weekly Recurring</span>
              </span>
            )}
            <span
              className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                ride.status === 'Open'
                  ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800'
                  : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300'
              }`}
            >
              {ride.status || 'Open'}
            </span>
          </div>

          {/* Route path */}
          <div className="space-y-2">
            <div className="flex items-start space-x-3">
              <div className="mt-1 flex flex-col items-center">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 ring-4 ring-emerald-100 dark:ring-emerald-950"></span>
                <span className="w-0.5 h-6 bg-slate-200 dark:bg-slate-700 my-0.5"></span>
                <MapPin className="w-4 h-4 text-rose-500" />
              </div>
              <div className="space-y-2 flex-1">
                <div>
                  <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Origin</span>
                  <p className="text-sm sm:text-base font-bold text-slate-900 dark:text-white leading-tight">
                    {ride.origin}
                  </p>
                </div>
                <div>
                  <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Destination</span>
                  <p className="text-sm sm:text-base font-bold text-slate-900 dark:text-white leading-tight">
                    {ride.destination}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Departure & Seat info */}
        <div className="flex flex-row md:flex-col items-center md:items-end justify-between border-t md:border-t-0 md:border-l border-slate-100 dark:border-slate-800 pt-4 md:pt-0 md:pl-6 gap-3">
          <div className="space-y-1.5 md:text-right">
            <div className="flex items-center md:justify-end space-x-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              <span>{formatDeparture(ride.departureTime)}</span>
            </div>

            <div className="flex items-center md:justify-end space-x-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300">
              <Users className="w-3.5 h-3.5 text-slate-400" />
              <span className="text-emerald-600 dark:text-emerald-400 font-bold">{ride.availableSeats}</span>
              <span>{ride.availableSeats === 1 ? 'seat left' : 'seats left'}</span>
            </div>
          </div>

          {/* Action button */}
          <div className="mt-2">
            {isDriver ? (
              <span className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-semibold bg-indigo-50 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800">
                Offered by You
              </span>
            ) : requestStatus === 'success' ? (
              <span className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-xl text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Requested!</span>
              </span>
            ) : (
              <button
                onClick={handleRequestSeat}
                disabled={isRequesting || ride.availableSeats <= 0}
                className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 dark:bg-emerald-600 dark:hover:bg-emerald-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm"
              >
                {isRequesting ? 'Requesting...' : ride.availableSeats <= 0 ? 'Full' : 'Request Seat'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Error alert if request fails */}
      {requestStatus === 'error' && (
        <div className="mt-3 p-2.5 rounded-xl bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-800 text-xs text-red-700 dark:text-red-300 flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}
    </div>
  );
};

