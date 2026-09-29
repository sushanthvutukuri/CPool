import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Car,
  Mail,
  Phone,
  ArrowLeft,
  ShieldCheck,
  Calendar,
  Sparkles,
  MapPin,
  Leaf,
  Award,
  LogOut,
  Plus,
  Compass,
  CheckCircle2,
} from 'lucide-react';
import { Navbar } from '../components/Navbar';
import { CreateRideModal } from '../components/CreateRideModal';
import { useAuth } from '../context/AuthContext';
import { rideApi } from '../api/rideApi';
import { rideRequestApi } from '../api/rideRequestApi';
import { Ride, RideRequest } from '../types';

export const ProfilePage: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [myRides, setMyRides] = useState<Ride[]>([]);
  const [myRequests, setMyRequests] = useState<RideRequest[]>([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      setIsLoading(true);
      try {
        const [allRides, requests] = await Promise.all([
          rideApi.getAllRides().catch(() => []),
          user?.id
            ? rideRequestApi.getAllRideRequests(undefined, user.id).catch(() => [])
            : Promise.resolve([]),
        ]);

        const userOffered = allRides.filter((r) => r.driverId === user?.id);
        setMyRides(userOffered);
        setMyRequests(requests);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserData();
  }, [user?.id]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userInitial = user?.name ? user.name.charAt(0).toUpperCase() : 'U';
  const totalOffered = myRides.length;
  const totalJoined = myRequests.length;
  const totalTrips = totalOffered + totalJoined;

  // Approximate eco impact estimate
  const carbonSavedKg = (totalTrips * 4.2).toFixed(1);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col transition-colors duration-200">
      {/* Top Navigation */}
      <Navbar onOpenCreateRide={() => setIsCreateModalOpen(true)} />

      {/* Main Profile Container */}
      <main className="flex-1 max-w-5xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Back Link */}
        <div>
          <Link
            to="/"
            className="inline-flex items-center space-x-2 text-xs font-bold text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Dashboard</span>
          </Link>
        </div>

        {/* Profile Banner Card */}
        <div className="relative bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 shadow-sm overflow-hidden">
          {/* Gradient Banner Cover */}
          <div className="h-36 sm:h-44 bg-gradient-to-r from-slate-900 via-emerald-950 to-teal-900 relative">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(34,197,94,0.15),transparent_50%)]" />
            <div className="absolute top-4 right-4 sm:top-6 sm:right-6">
              <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-white/10 backdrop-blur-md text-emerald-300 border border-white/10">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                <span>Verified Comet Commuter</span>
              </span>
            </div>
          </div>

          {/* User Details Header */}
          <div className="px-6 sm:px-8 pb-8 pt-4 relative">
            <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-6">
              {/* Avatar & Name Info */}
              <div className="flex flex-col sm:flex-row items-center sm:items-end space-y-3 sm:space-y-0 sm:space-x-5 text-center sm:text-left">
                {/* Avatar with negative top margin only */}
                <div className="relative group -mt-16 sm:-mt-20">
                  <div className="w-28 h-28 sm:w-32 sm:h-32 rounded-3xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center text-4xl sm:text-5xl font-black shadow-xl ring-4 ring-white dark:ring-slate-900">
                    {userInitial}
                  </div>
                  <span
                    className="absolute bottom-1 right-1 w-5 h-5 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-slate-900"
                    title="Online & Active"
                  />
                </div>

                {/* Name & Role cleanly positioned in the card body */}
                <div className="space-y-1 sm:pb-2">
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight">
                    {user?.name || 'Comet Rider'}
                  </h1>
                  <p className="text-xs sm:text-sm font-semibold text-emerald-600 dark:text-emerald-400">
                    UT Dallas Campus Driver & Passenger
                  </p>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-center sm:justify-end space-x-3 sm:pb-2">
                <button
                  onClick={() => setIsCreateModalOpen(true)}
                  className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md shadow-emerald-600/20 transition-all duration-200"
                >
                  <Plus className="w-4 h-4" />
                  <span>Offer Ride</span>
                </button>
                <button
                  onClick={handleLogout}
                  className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-red-50 text-slate-700 hover:text-red-600 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-red-950/40 dark:hover:text-red-400 font-bold text-xs transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Sign Out</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Stats Grid */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-5 shadow-sm text-center space-y-1">
            <div className="w-10 h-10 mx-auto rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <Car className="w-5 h-5" />
            </div>
            <span className="block text-2xl font-black text-slate-900 dark:text-white">
              {isLoading ? '...' : totalOffered}
            </span>
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Rides Offered
            </span>
          </div>

          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-5 shadow-sm text-center space-y-1">
            <div className="w-10 h-10 mx-auto rounded-2xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center">
              <Compass className="w-5 h-5" />
            </div>
            <span className="block text-2xl font-black text-slate-900 dark:text-white">
              {isLoading ? '...' : totalJoined}
            </span>
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Trips Joined
            </span>
          </div>

          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-5 shadow-sm text-center space-y-1">
            <div className="w-10 h-10 mx-auto rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center justify-center">
              <Leaf className="w-5 h-5" />
            </div>
            <span className="block text-2xl font-black text-slate-900 dark:text-white">
              {isLoading ? '...' : `${carbonSavedKg} kg`}
            </span>
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Carbon Offset
            </span>
          </div>

          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-5 shadow-sm text-center space-y-1">
            <div className="w-10 h-10 mx-auto rounded-2xl bg-amber-50 dark:bg-amber-950/60 text-amber-500 flex items-center justify-center">
              <Award className="w-5 h-5" />
            </div>
            <span className="block text-2xl font-black text-slate-900 dark:text-white">
              5.0 ★
            </span>
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Comet Rating
            </span>
          </div>
        </div>

        {/* Details & Preferences Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Contact & Account Information */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-6 sm:p-7 shadow-sm space-y-6">
            <div className="flex items-center space-x-3 border-b border-slate-100 dark:border-slate-800 pb-4">
              <div className="p-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                <Mail className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Account Details</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">Your campus contact information</p>
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Full Name</span>
                <p className="text-sm font-semibold text-slate-900 dark:text-white">
                  {user?.name || 'Not provided'}
                </p>
              </div>

              <div className="space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Email Address</span>
                <div className="flex items-center space-x-2">
                  <p className="text-sm font-semibold text-slate-900 dark:text-white">
                    {user?.email || 'Not provided'}
                  </p>
                  <span className="inline-flex items-center text-xs font-bold text-emerald-600 dark:text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5 mr-0.5" />
                    Verified
                  </span>
                </div>
              </div>

              <div className="space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Phone Number</span>
                <div className="flex items-center space-x-2">
                  {user?.phoneNumber ? (
                    <a
                      href={`tel:${user.phoneNumber}`}
                      className="text-sm font-semibold text-emerald-600 dark:text-emerald-400 hover:underline flex items-center space-x-1.5"
                    >
                      <Phone className="w-3.5 h-3.5" />
                      <span>{user.phoneNumber}</span>
                    </a>
                  ) : (
                    <p className="text-sm text-slate-400 italic">No phone number added</p>
                  )}
                </div>
              </div>

              <div className="space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Campus Affiliation</span>
                <p className="text-sm font-semibold text-slate-900 dark:text-white">
                  University of Texas at Dallas
                </p>
              </div>
            </div>
          </div>

          {/* Commute Preferences & Campus Hubs */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 p-6 sm:p-7 shadow-sm space-y-6">
            <div className="flex items-center space-x-3 border-b border-slate-100 dark:border-slate-800 pb-4">
              <div className="p-2.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
                <Sparkles className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Commute Highlights</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">Your frequent routes & preferences</p>
              </div>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Favorite Pick-up Spots</span>
                <div className="flex flex-wrap gap-2">
                  <span className="inline-flex items-center space-x-1 px-3 py-1 rounded-xl text-xs font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                    <MapPin className="w-3.5 h-3.5 text-emerald-500" />
                    <span>UTD Student Union</span>
                  </span>
                  <span className="inline-flex items-center space-x-1 px-3 py-1 rounded-xl text-xs font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                    <MapPin className="w-3.5 h-3.5 text-emerald-500" />
                    <span>ECS South</span>
                  </span>
                  <span className="inline-flex items-center space-x-1 px-3 py-1 rounded-xl text-xs font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                    <MapPin className="w-3.5 h-3.5 text-emerald-500" />
                    <span>Northside Apartments</span>
                  </span>
                </div>
              </div>

              <div className="space-y-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Preferred Travel Times</span>
                <div className="flex items-center space-x-2 text-xs font-semibold text-slate-700 dark:text-slate-300">
                  <Calendar className="w-4 h-4 text-slate-400" />
                  <span>Weekdays: 8:00 AM – 9:30 AM & 4:30 PM – 6:00 PM</span>
                </div>
              </div>

              <div className="space-y-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Ride Vibes</span>
                <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-700/60 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                  Friendly conversations, lo-fi beats, clean car, and punctual departures.
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Create Ride Modal */}
      <CreateRideModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={() => {
          navigate('/');
        }}
      />
    </div>
  );
};