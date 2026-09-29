package com.utd.cpool.service;

import com.utd.cpool.dto.ride.CreateRideRequest;
import com.utd.cpool.dto.ride.RideResponse;
import com.utd.cpool.dto.ride.UpdateRideRequest;
import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.RideAlreadyExistsException;
import com.utd.cpool.exception.RideNotFoundException;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.RideRepository;
import com.utd.cpool.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public RideService(RideRepository rideRepository, UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    public List<RideResponse> createRide(CreateRideRequest request) {
        User driver = userRepository.findById(request.driverId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.driverId()));

        LocalDateTime startDateTime = parseDepartureTime(request.departureTime());

        // Check if an identical ride already exists for this driver at this time
        if (rideRepository.existsByDriverIDAndDepartureTime(driver, startDateTime)) {
            throw new RideAlreadyExistsException("A ride already exists for this driver at " + startDateTime);
        }

        boolean isRecurring = Boolean.TRUE.equals(request.recurring());

        // If recurring is true, ensure none of the upcoming weekly recurring slots already exist
        if (isRecurring) {
            for (int i = 1; i <= 4; i++) {
                LocalDateTime recurringTime = startDateTime.plusWeeks(i);
                if (rideRepository.existsByDriverIDAndDepartureTime(driver, recurringTime)) {
                    throw new RideAlreadyExistsException("A ride already exists for this driver at " + recurringTime);
                }
            }
        }

        List<Ride> ridesToSave = new ArrayList<>();

        // Create the initial ride
        Ride initialRide = new Ride();
        initialRide.setDriverID(driver);
        initialRide.setOrigin(request.origin());
        initialRide.setDestination(request.destination());
        initialRide.setDepartureTime(startDateTime);
        initialRide.setAvailableSeats(request.availableSeats());
        initialRide.setRecurring(isRecurring);
        ridesToSave.add(initialRide);

        // If recurring is true, automatically create weekly rides for the next month (4 weeks)
        if (isRecurring) {
            for (int i = 1; i <= 4; i++) {
                Ride recurringRide = new Ride();
                recurringRide.setDriverID(driver);
                recurringRide.setOrigin(request.origin());
                recurringRide.setDestination(request.destination());
                recurringRide.setDepartureTime(startDateTime.plusWeeks(i));
                recurringRide.setAvailableSeats(request.availableSeats());
                recurringRide.setRecurring(true);
                ridesToSave.add(recurringRide);
            }
        }

        List<Ride> savedRides = rideRepository.saveAll(ridesToSave);
        return savedRides.stream()
                .map(this::mapToRideResponse)
                .toList();
    }

    public LocalDateTime parseDepartureTime(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Departure time cannot be blank");
        }

        String trimmed = input.trim();

        // 1. Try ISO format (e.g. "2026-01-18T20:00:00" or "2026-01-18T20:00")
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {}

        // 2. Formats with date and time (e.g. "January 18, 2026 8:00PM", "Jan 18, 2026 8:00 PM")
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("[MMMM][MMM] d, yyyy[ ]h:mm[ ]a")
                .toFormatter(Locale.ENGLISH),

            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("[MMMM][MMM] d, yyyy[ ]hh:mm[ ]a")
                .toFormatter(Locale.ENGLISH),

            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("[MMMM][MMM] d, yyyy[ ]H:mm[:ss]")
                .toFormatter(Locale.ENGLISH),

            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("yyyy-MM-dd[ ]H:mm[:ss]")
                .toFormatter(Locale.ENGLISH),

            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("M/d/yyyy[ ]h:mm[ ]a")
                .toFormatter(Locale.ENGLISH),

            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("M/d/yyyy[ ]H:mm[:ss]")
                .toFormatter(Locale.ENGLISH)
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {}
        }

        // 3. Formats with time only (e.g. "8:00PM", "20:00") -> defaults to current date
        DateTimeFormatter[] timeFormatters = new DateTimeFormatter[] {
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mm[ ]a").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh:mm[ ]a").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("H:mm[:ss]").toFormatter(Locale.ENGLISH)
        };

        for (DateTimeFormatter timeFormatter : timeFormatters) {
            try {
                LocalTime time = LocalTime.parse(trimmed, timeFormatter);
                return LocalDateTime.of(LocalDate.now(), time);
            } catch (DateTimeParseException ignored) {}
        }

        throw new IllegalArgumentException("Unable to parse departure time: '" + input + 
                "'. Expected formats such as 'January 18, 2026 8:00PM' or '2026-01-18T20:00:00'");
    }

    public RideResponse getRide(UUID id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + id));
        return mapToRideResponse(ride);
    }

    public List<RideResponse> getAllRides() {
        return rideRepository.findAll()
                .stream()
                .map(this::mapToRideResponse)
                .toList();
    }

    public RideResponse updateRide(UpdateRideRequest request)
    {
        Ride updatedRide=new Ride();
        updatedRide.setAvailableSeats(request.availableSeats());
        updatedRide.setId(updatedRide.getId());
        updatedRide.setDepartureTime(request.departureTime());
        return mapToRideResponse(updatedRide);
    }

    public RideResponse mapToRideResponse(Ride ride) {
        UUID driverId = ride.getDriverID() != null ? ride.getDriverID().getId() : null;
        String driverPhone = ride.getDriverID() != null ? ride.getDriverID().getPhoneNumber() : null;
        return new RideResponse(
                ride.getId(),
                driverId,
                driverPhone,
                ride.getOrigin(),
                ride.getDestination(),
                ride.getDepartureTime(),
                ride.getAvailableSeats(),
                ride.getStatus(),
                ride.getCreatedAt(),
                ride.isRecurring()
        );
    }
}
