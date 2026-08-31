import React, { useState, useMemo } from 'react';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Repeat, Sparkles, User, Globe } from 'lucide-react';
import { Ride } from '../types';

interface CalendarViewProps {
  rides: Ride[];
  selectedDate: Date | null;
  onSelectDate: (date: Date | null) => void;
  viewScope: 'my' | 'all';
  onToggleViewScope: (scope: 'my' | 'all') => void;
  myRidesCount?: number;
  allRidesCount?: number;
}

export const CalendarView: React.FC<CalendarViewProps> = ({
  rides,
  selectedDate,
  onSelectDate,
  viewScope,
  onToggleViewScope,
  myRidesCount = 0,
  allRidesCount = 0,
}) => {
  const [currentMonth, setCurrentMonth] = useState<Date>(() => new Date());

  // Group rides by date string (YYYY-MM-DD)
  const ridesByDate = useMemo(() => {
    const map = new Map<string, { total: number; hasRecurring: boolean; rides: Ride[] }>();
    rides.forEach((ride) => {
      try {
        const d = new Date(ride.departureTime);
        if (!isNaN(d.getTime())) {
          const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
          const current = map.get(key) || { total: 0, hasRecurring: false, rides: [] };
          current.total += 1;
          if (ride.recurring) current.hasRecurring = true;
          current.rides.push(ride);
          map.set(key, current);
        }
      } catch (e) {
        console.warn('Failed to parse departure date:', ride.departureTime);
      }
    });
    return map;
  }, [rides]);

  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();

  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayOfWeek = new Date(year, month, 1).getDay(); // 0 = Sunday

  const prevMonth = () => {
    setCurrentMonth(new Date(year, month - 1, 1));
  };

  const nextMonth = () => {
    setCurrentMonth(new Date(year, month + 1, 1));
  };

  const goToToday = () => {
    const today = new Date();
    setCurrentMonth(new Date(today.getFullYear(), today.getMonth(), 1));
    onSelectDate(today);
  };

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  // Helpers to compare dates
  const isSameDay = (d1: Date | null, d2: Date | null) => {
    if (!d1 || !d2) return false;
    return (
      d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
    );
  };

  const today = new Date();

  return (
    <div className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800/80 p-5 sm:p-6 shadow-sm space-y-5">
      {/* Calendar Scope Switcher Toggle */}
      <div className="bg-slate-100 dark:bg-slate-800/80 p-1 rounded-2xl flex items-center justify-between border border-slate-200/60 dark:border-slate-700/60">
        <button
          onClick={() => onToggleViewScope('my')}
          className={`flex-1 flex items-center justify-center space-x-2 py-2 px-3 rounded-xl text-xs font-bold transition-all duration-200 ${
            viewScope === 'my'
              ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-white shadow-sm border border-slate-200/60 dark:border-slate-700'
              : 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          <User className="w-3.5 h-3.5" />
          <span>My Schedule</span>
          <span className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
            viewScope === 'my'
              ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
              : 'bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-400'
          }`}>
            {myRidesCount}
          </span>
        </button>

        <button
          onClick={() => onToggleViewScope('all')}
          className={`flex-1 flex items-center justify-center space-x-2 py-2 px-3 rounded-xl text-xs font-bold transition-all duration-200 ${
            viewScope === 'all'
              ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-white shadow-sm border border-slate-200/60 dark:border-slate-700'
              : 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200'
          }`}
        >
          <Globe className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
          <span>All Campus Rides</span>
          <span className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
            viewScope === 'all'
              ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
              : 'bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-400'
          }`}>
            {allRidesCount}
          </span>
        </button>
      </div>

      {/* Calendar Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
            <CalendarIcon className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white leading-tight">
              {monthNames[month]} {year}
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              {viewScope === 'my' ? 'Viewing your scheduled rides' : 'Viewing all available campus rides'}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          {selectedDate && (
            <button
              onClick={() => onSelectDate(null)}
              className="text-xs font-semibold px-2.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
            >
              Reset
            </button>
          )}
          <button
            onClick={goToToday}
            className="text-xs font-semibold px-2.5 py-1.5 rounded-lg text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/40 hover:bg-emerald-100 dark:hover:bg-emerald-900/40 transition-colors"
          >
            Today
          </button>
          <div className="flex items-center space-x-1 border border-slate-200 dark:border-slate-700 rounded-xl p-0.5">
            <button
              onClick={prevMonth}
              aria-label="Previous month"
              className="p-1.5 rounded-lg text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={nextMonth}
              aria-label="Next month"
              className="p-1.5 rounded-lg text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Days of Week Header */}
      <div className="grid grid-cols-7 gap-1 sm:gap-2 text-center">
        {daysOfWeek.map((day, idx) => (
          <span
            key={day}
            className={`text-[11px] font-bold uppercase tracking-wider py-1 ${
              idx === 0 || idx === 6
                ? 'text-slate-400 dark:text-slate-500'
                : 'text-slate-600 dark:text-slate-400'
            }`}
          >
            {day}
          </span>
        ))}
      </div>

      {/* Days Grid */}
      <div className="grid grid-cols-7 gap-1 sm:gap-2">
        {/* Leading blank slots */}
        {Array.from({ length: firstDayOfWeek }).map((_, index) => (
          <div
            key={`blank-${index}`}
            className="h-12 sm:h-16 rounded-2xl bg-slate-50/50 dark:bg-slate-900/40 border border-transparent"
          />
        ))}

        {/* Days in current month */}
        {Array.from({ length: daysInMonth }).map((_, index) => {
          const dayNumber = index + 1;
          const cellDate = new Date(year, month, dayNumber);
          const key = `${year}-${String(month + 1).padStart(2, '0')}-${String(dayNumber).padStart(2, '0')}`;
          const rideInfo = ridesByDate.get(key);
          const isCurrentDay = isSameDay(cellDate, today);
          const isSelected = isSameDay(cellDate, selectedDate);

          return (
            <button
              key={dayNumber}
              onClick={() => onSelectDate(isSelected ? null : cellDate)}
              className={`group relative h-12 sm:h-16 rounded-2xl p-1.5 sm:p-2 flex flex-col justify-between items-start transition-all duration-200 border ${
                isSelected
                  ? 'bg-emerald-600 dark:bg-emerald-600 text-white border-emerald-600 shadow-md shadow-emerald-600/30 ring-2 ring-emerald-500/50'
                  : isCurrentDay
                  ? 'bg-emerald-50/80 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-300 border-emerald-300 dark:border-emerald-700/60 hover:border-emerald-400'
                  : 'bg-slate-50/70 dark:bg-slate-850/60 text-slate-700 dark:text-slate-300 border-slate-200/60 dark:border-slate-800 hover:bg-slate-100/90 dark:hover:bg-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
              }`}
            >
              {/* Day Number */}
              <div className="w-full flex items-center justify-between">
                <span
                  className={`text-xs sm:text-sm font-bold ${
                    isSelected
                      ? 'text-white'
                      : isCurrentDay
                      ? 'text-emerald-700 dark:text-emerald-400 font-extrabold'
                      : 'text-slate-800 dark:text-slate-200'
                  }`}
                >
                  {dayNumber}
                </span>

                {/* Recurring Indicator Badge */}
                {rideInfo?.hasRecurring && (
                  <span
                    title="Includes Recurring Rides"
                    className={`inline-flex items-center ${
                      isSelected
                        ? 'text-emerald-100'
                        : 'text-teal-600 dark:text-teal-400'
                    }`}
                  >
                    <Repeat className="w-3 h-3" />
                  </span>
                )}
              </div>

              {/* Ride Counter Indicator */}
              {rideInfo && (
                <div className="w-full flex items-center justify-between mt-auto">
                  <span
                    className={`text-[10px] sm:text-xs font-semibold px-1.5 py-0.5 rounded-md truncate ${
                      isSelected
                        ? 'bg-white/20 text-white'
                        : 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/60 dark:text-emerald-200'
                    }`}
                  >
                    {rideInfo.total} {rideInfo.total === 1 ? 'ride' : 'rides'}
                  </span>
                </div>
              )}
            </button>
          );
        })}
      </div>

      {/* Calendar Legend */}
      <div className="pt-4 border-t border-slate-100 dark:border-slate-800/80 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-500 dark:text-slate-400">
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            <span>Rides Available</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <Repeat className="w-3 h-3 text-teal-600 dark:text-teal-400" />
            <span>Recurring</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 ring-2 ring-emerald-300"></span>
            <span>Today</span>
          </div>
        </div>

        <div className="flex items-center space-x-1 text-slate-400">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Click any date to filter</span>
        </div>
      </div>
    </div>
  );
};
