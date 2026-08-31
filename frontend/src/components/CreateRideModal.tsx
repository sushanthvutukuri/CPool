import React, { useState } from 'react';
import { X, MapPin, Clock, Users, Repeat, AlertCircle, Sparkles } from 'lucide-react';
import { rideApi } from '../api/rideApi';
import { useAuth } from '../context/AuthContext';

interface CreateRideModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const CreateRideModal: React.FC<CreateRideModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { user } = useAuth();

  const [origin, setOrigin] = useState('UTD Student Union');
  const [destination, setDestination] = useState('DFW Airport');
  
  // Default departure date to tomorrow at 8:00 AM
  const getTomorrowDefault = () => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}T08:00`;
  };

  const [departureTime, setDepartureTime] = useState(getTomorrowDefault());
  const [availableSeats, setAvailableSeats] = useState<number>(3);
  const [recurring, setRecurring] = useState<boolean>(false);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const quickOrigins = ['UTD Student Union', 'ECS South', 'Northside Apartments', 'Canyon Creek'];
  const quickDestinations = ['DFW Airport', 'Dallas Love Field', 'UTD Campus', 'Dallas Downtown'];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    if (!origin.trim() || !destination.trim() || !departureTime.trim()) {
      setError('Please fill in all required fields.');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await rideApi.createRide({
        driverId: user.id,
        origin: origin.trim(),
        destination: destination.trim(),
        departureTime: departureTime.trim(),
        availableSeats,
        recurring,
      });

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to create ride. Please verify your inputs.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-lg bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Offer a New Ride</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Share your commute with fellow campus riders</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {error && (
            <div className="p-3 rounded-2xl bg-red-50 dark:bg-red-950/50 border border-red-200 dark:border-red-800 text-xs text-red-700 dark:text-red-300 flex items-center space-x-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Origin */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Pick-up Location (Origin)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-emerald-500">
                <MapPin className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={origin}
                onChange={(e) => setOrigin(e.target.value)}
                placeholder="e.g. UTD Student Union"
                required
                className="w-full pl-10 pr-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-slate-900 dark:text-white placeholder-slate-400"
              />
            </div>
            {/* Quick Origin Presets */}
            <div className="flex flex-wrap gap-1.5 pt-1">
              {quickOrigins.map((loc) => (
                <button
                  type="button"
                  key={loc}
                  onClick={() => setOrigin(loc)}
                  className="text-[11px] font-semibold px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-emerald-50 hover:text-emerald-700 dark:hover:bg-emerald-950/40 dark:hover:text-emerald-300 transition-colors"
                >
                  {loc}
                </button>
              ))}
            </div>
          </div>

          {/* Destination */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Drop-off Location (Destination)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-rose-500">
                <MapPin className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                placeholder="e.g. DFW Airport"
                required
                className="w-full pl-10 pr-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-slate-900 dark:text-white placeholder-slate-400"
              />
            </div>
            {/* Quick Destination Presets */}
            <div className="flex flex-wrap gap-1.5 pt-1">
              {quickDestinations.map((loc) => (
                <button
                  type="button"
                  key={loc}
                  onClick={() => setDestination(loc)}
                  className="text-[11px] font-semibold px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-emerald-50 hover:text-emerald-700 dark:hover:bg-emerald-950/40 dark:hover:text-emerald-300 transition-colors"
                >
                  {loc}
                </button>
              ))}
            </div>
          </div>

          {/* Departure Date/Time & Seats Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                Departure Time
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Clock className="w-4 h-4" />
                </div>
                <input
                  type="datetime-local"
                  value={departureTime}
                  onChange={(e) => setDepartureTime(e.target.value)}
                  required
                  className="w-full pl-10 pr-3 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-slate-900 dark:text-white"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                Available Seats
              </label>
              <div className="relative flex items-center">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Users className="w-4 h-4" />
                </div>
                <input
                  type="number"
                  min="1"
                  max="8"
                  value={availableSeats}
                  onChange={(e) => setAvailableSeats(parseInt(e.target.value) || 1)}
                  required
                  className="w-full pl-10 pr-3 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-slate-900 dark:text-white"
                />
              </div>
            </div>
          </div>

          {/* Recurring Toggle Card */}
          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700 flex items-start justify-between space-x-3">
            <div className="flex items-start space-x-3">
              <div className="p-2 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 mt-0.5">
                <Repeat className="w-4 h-4" />
              </div>
              <div className="space-y-0.5">
                <label htmlFor="recurring-toggle" className="text-sm font-bold text-slate-900 dark:text-white cursor-pointer">
                  Recurring Ride (Next Month)
                </label>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Automatically schedules this ride weekly for the next 4 weeks.
                </p>
              </div>
            </div>
            <input
              id="recurring-toggle"
              type="checkbox"
              checked={recurring}
              onChange={(e) => setRecurring(e.target.checked)}
              className="mt-1 w-5 h-5 text-emerald-600 rounded-lg border-slate-300 dark:border-slate-600 focus:ring-emerald-500 dark:bg-slate-700 cursor-pointer"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end space-x-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-6 py-2.5 rounded-xl text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-500 active:bg-emerald-700 shadow-md shadow-emerald-600/30 transition-all duration-200 disabled:opacity-50 flex items-center space-x-2"
            >
              {isLoading ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  <span>Publishing...</span>
                </>
              ) : (
                <span>Publish Ride</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
