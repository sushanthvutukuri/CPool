import React, { useState, useEffect, useMemo } from 'react';
import { Navbar } from '../components/Navbar';
import { CalendarView } from '../components/CalendarView';
import { RideCard } from '../components/RideCard';
import { CreateRideModal } from '../components/CreateRideModal';
import { Ride, RideRequest } from '../types';
import { rideApi } from '../api/rideApi';
import { rideRequestApi } from '../api/rideRequestApi';
import { useAuth } from '../context/AuthContext';
import { Search, Plus, Car, Filter, RefreshCw, ShieldCheck, Compass, UserCheck } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  const [allRides, setAllRides] = useState<Ride[]>([]);
  const [myRequests, setMyRequests] = useState<RideRequest[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Calendar Scope: 'my' (My Schedule) vs 'all' (All Campus Rides)
  const [calendarScope, setCalendarScope] = useState<'my' | 'all'>('my');

  // Tab options: 'my-rides' (Default: User's upcoming rides), 'driver', 'passenger', 'browse'
  const [activeTab, setActiveTab] = useState<'my-rides' | 'driver' | 'passenger' | 'browse'>('my-rides');

  const fetchData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [ridesData, requestsData] = await Promise.all([
        rideApi.getAllRides(),
        user?.id ? rideRequestApi.getAllRideRequests(undefined, user.id).catch(() => []) : Promise.resolve([]),
      ]);
      setAllRides(ridesData);
      setMyRequests(requestsData);
    } catch (err: any) {
      setError(err.message || 'Failed to load rides. Please check backend connection.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user?.id]);

  // Derive user's rides
  const myRequestedRideIds = useMemo(() => {
    return new Set(myRequests.map((req) => req.rideId));
  }, [myRequests]);

  const myOfferedRides = useMemo(() => {
    return allRides.filter((r) => r.driverId === user?.id);
  }, [allRides, user?.id]);

  const myPassengerRides = useMemo(() => {
    return allRides.filter((r) => myRequestedRideIds.has(r.id));
  }, [allRides, myRequestedRideIds]);

  // Combined User's Upcoming Rides (Driver + Passenger)
  const myUpcomingRides = useMemo(() => {
    return allRides.filter(
      (r) => r.driverId === user?.id || myRequestedRideIds.has(r.id)
    );
  }, [allRides, user?.id, myRequestedRideIds]);

  // Handle calendar scope toggling
  const handleToggleCalendarScope = (scope: 'my' | 'all') => {
    setCalendarScope(scope);
    if (scope === 'all' && activeTab !== 'browse') {
      setActiveTab('browse');
    } else if (scope === 'my' && activeTab === 'browse') {
      setActiveTab('my-rides');
    }
  };

  // Determine base rides for current view
  const baseRidesForView = useMemo(() => {
    if (activeTab === 'driver') return myOfferedRides;
    if (activeTab === 'passenger') return myPassengerRides;
    if (activeTab === 'browse') return allRides;
    return myUpcomingRides; // 'my-rides'
  }, [activeTab, myOfferedRides, myPassengerRides, allRides, myUpcomingRides]);

  // Calendar display rides based on chosen scope
  const calendarRides = calendarScope === 'all' ? allRides : myUpcomingRides;

  // Filter rides based on search query and selected date
  const filteredRides = useMemo(() => {
    return baseRidesForView.filter((ride) => {
      // 1. Search Query filter (Origin or Destination)
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const matchesOrigin = ride.origin.toLowerCase().includes(query);
        const matchesDest = ride.destination.toLowerCase().includes(query);
        if (!matchesOrigin && !matchesDest) return false;
      }

      // 2. Calendar Selected Date filter
      if (selectedDate) {
        try {
          const departure = new Date(ride.departureTime);
          const isSameDay =
            departure.getFullYear() === selectedDate.getFullYear() &&
            departure.getMonth() === selectedDate.getMonth() &&
            departure.getDate() === selectedDate.getDate();
          if (!isSameDay) return false;
        } catch {
          return false;
        }
      }

      return true;
    });
  }, [baseRidesForView, searchQuery, selectedDate]);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col transition-colors duration-200">
      {/* Top Navigation */}
      <Navbar onOpenCreateRide={() => setIsCreateModalOpen(true)} />

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Welcome Banner & User Stats */}
        <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-emerald-950 text-white rounded-3xl p-6 sm:p-8 shadow-xl shadow-slate-900/10 dark:shadow-none flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-xl">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Verified Campus Commuters</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Hello, {user?.name || 'Comet Rider'} 👋
            </h1>
            <p className="text-sm text-slate-300">
              Manage your personal commute schedule or explore all available rides across campus.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3 sm:gap-4">
            <div className="bg-white/10 backdrop-blur-md px-4 py-3 rounded-2xl border border-white/10 text-center min-w-[100px]">
              <span className="block text-2xl font-black text-emerald-400">{myUpcomingRides.length}</span>
              <span className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">My Rides</span>
            </div>
            <div className="bg-white/10 backdrop-blur-md px-4 py-3 rounded-2xl border border-white/10 text-center min-w-[100px]">
              <span className="block text-2xl font-black text-teal-300">{myOfferedRides.length}</span>
              <span className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">As Driver</span>
            </div>
            <div className="bg-white/10 backdrop-blur-md px-4 py-3 rounded-2xl border border-white/10 text-center min-w-[100px]">
              <span className="block text-2xl font-black text-amber-400">{allRides.length}</span>
              <span className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">Campus Total</span>
            </div>
          </div>
        </div>

        {/* Calendar and Rides Grid Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left Column: Interactive Calendar with Scope Toggle (5 cols on large screens) */}
          <div className="lg:col-span-5 space-y-6">
            <CalendarView
              rides={calendarRides}
              selectedDate={selectedDate}
              onSelectDate={setSelectedDate}
              viewScope={calendarScope}
              onToggleViewScope={handleToggleCalendarScope}
              myRidesCount={myUpcomingRides.length}
              allRidesCount={allRides.length}
            />

            {/* Quick Helper Card */}
            <div className="bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 rounded-3xl p-5 text-xs text-emerald-900 dark:text-emerald-300 space-y-2">
              <div className="flex items-center space-x-2 font-bold text-sm">
                <Car className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                <span>Calendar View Switcher</span>
              </div>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Use the toggle buttons at the top of the calendar to switch between <strong>My Schedule</strong> (your personal rides) and <strong>All Campus Rides</strong> (all available trips on campus).
              </p>
            </div>
          </div>

          {/* Right Column: User Rides Feed and Filters (7 cols on large screens) */}
          <div className="lg:col-span-7 space-y-6">
            {/* Search and Action Bar */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
              {/* Search Bar */}
              <div className="relative flex-1">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Search className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search rides by origin or destination..."
                  className="w-full pl-10 pr-4 py-2.5 rounded-2xl text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-slate-900 dark:text-white placeholder-slate-400 shadow-sm"
                />
              </div>

              {/* Refresh Button */}
              <button
                onClick={fetchData}
                title="Refresh schedule"
                className="p-2.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white shadow-sm hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors flex items-center justify-center"
              >
                <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              </button>

              {/* Mobile "Offer Ride" CTA */}
              <button
                onClick={() => setIsCreateModalOpen(true)}
                className="sm:hidden flex items-center justify-center space-x-2 px-4 py-2.5 rounded-2xl bg-emerald-600 text-white font-bold text-sm shadow-md"
              >
                <Plus className="w-4 h-4" />
                <span>Offer a Ride</span>
              </button>
            </div>

            {/* Filter Tabs */}
            <div className="flex items-center space-x-2 border-b border-slate-200 dark:border-slate-800 pb-3 overflow-x-auto">
              <button
                onClick={() => {
                  setActiveTab('my-rides');
                  setCalendarScope('my');
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-colors whitespace-nowrap flex items-center space-x-1.5 ${
                  activeTab === 'my-rides'
                    ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <UserCheck className="w-3.5 h-3.5" />
                <span>My Upcoming Rides ({myUpcomingRides.length})</span>
              </button>

              <button
                onClick={() => {
                  setActiveTab('driver');
                  setCalendarScope('my');
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-colors whitespace-nowrap ${
                  activeTab === 'driver'
                    ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                Offered by Me ({myOfferedRides.length})
              </button>

              <button
                onClick={() => {
                  setActiveTab('passenger');
                  setCalendarScope('my');
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-colors whitespace-nowrap ${
                  activeTab === 'passenger'
                    ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                Joined Trips ({myPassengerRides.length})
              </button>

              <button
                onClick={() => {
                  setActiveTab('browse');
                  setCalendarScope('all');
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-colors whitespace-nowrap flex items-center space-x-1.5 ${
                  activeTab === 'browse'
                    ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30'
                    : 'text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/40 hover:bg-emerald-100 dark:hover:bg-emerald-900/40'
                }`}
              >
                <Compass className="w-3.5 h-3.5" />
                <span>All Campus Rides ({allRides.length})</span>
              </button>
            </div>

            {/* Active Date Filter Chip */}
            {selectedDate && (
              <div className="flex items-center justify-between p-3 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-xs text-emerald-800 dark:text-emerald-300">
                <div className="flex items-center space-x-2">
                  <Filter className="w-3.5 h-3.5" />
                  <span>
                    Showing {calendarScope === 'my' ? 'your rides' : 'all campus rides'} departing on{' '}
                    <strong>
                      {selectedDate.toLocaleDateString('en-US', {
                        weekday: 'short',
                        month: 'short',
                        day: 'numeric',
                      })}
                    </strong>
                  </span>
                </div>
                <button
                  onClick={() => setSelectedDate(null)}
                  className="font-bold underline hover:text-emerald-950 dark:hover:text-white"
                >
                  Clear date filter
                </button>
              </div>
            )}

            {/* Rides List */}
            {isLoading && allRides.length === 0 ? (
              <div className="space-y-4">
                {[1, 2, 3].map((n) => (
                  <div
                    key={n}
                    className="h-36 rounded-2xl bg-slate-200 dark:bg-slate-800 animate-pulse"
                  />
                ))}
              </div>
            ) : error ? (
              <div className="p-6 rounded-3xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-center space-y-3">
                <p className="text-sm text-red-700 dark:text-red-300 font-semibold">{error}</p>
                <button
                  onClick={fetchData}
                  className="px-4 py-2 rounded-xl bg-red-600 text-white text-xs font-bold hover:bg-red-500"
                >
                  Try Again
                </button>
              </div>
            ) : filteredRides.length === 0 ? (
              <div className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-3xl p-10 text-center space-y-4 shadow-sm">
                <div className="w-14 h-14 mx-auto rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-400 flex items-center justify-center">
                  <Car className="w-7 h-7" />
                </div>
                <div className="space-y-1">
                  <h3 className="text-base font-bold text-slate-900 dark:text-white">
                    {activeTab === 'my-rides'
                      ? 'No upcoming rides scheduled'
                      : activeTab === 'driver'
                      ? 'You have not offered any rides yet'
                      : activeTab === 'passenger'
                      ? 'You have not joined any rides yet'
                      : 'No campus rides found matching your filters'}
                  </h3>
                  <p className="text-xs text-slate-500 dark:text-slate-400 max-w-sm mx-auto">
                    {activeTab === 'my-rides'
                      ? 'Offer a ride as a driver or switch to All Campus Rides to request a seat.'
                      : 'Try clearing your search or date filter to see more.'}
                  </p>
                </div>
                <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
                  <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md shadow-emerald-600/20 transition-colors"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Offer a Ride</span>
                  </button>
                  {activeTab !== 'browse' && (
                    <button
                      onClick={() => {
                        setActiveTab('browse');
                        setCalendarScope('all');
                      }}
                      className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 font-bold text-xs transition-colors"
                    >
                      <Compass className="w-4 h-4" />
                      <span>Explore All Campus Rides</span>
                    </button>
                  )}
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                {filteredRides.map((ride) => (
                  <RideCard
                    key={ride.id}
                    ride={ride}
                    onRequestSuccess={fetchData}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* Create Ride Modal */}
      <CreateRideModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={fetchData}
      />
    </div>
  );
};
