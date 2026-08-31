package com.utd.cpool.service;

import com.utd.cpool.dto.riderequest.CreateRideRequestDto;
import com.utd.cpool.dto.riderequest.RideRequestResponse;
import com.utd.cpool.dto.riderequest.UpdateRideRequestDto;
import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.RideRequest;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.RideNotFoundException;
import com.utd.cpool.exception.RideRequestAlreadyExistsException;
import com.utd.cpool.exception.RideRequestNotFoundException;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.RideRepository;
import com.utd.cpool.repository.RideRequestRepository;
import com.utd.cpool.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RideRequestService {

    private final RideRequestRepository rideRequestRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public RideRequestService(RideRequestRepository rideRequestRepository,
                              RideRepository rideRepository,
                              UserRepository userRepository) {
        this.rideRequestRepository = rideRequestRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    public RideRequestResponse createRideRequest(CreateRideRequestDto request) {
        Ride ride = rideRepository.findById(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + request.rideId()));

        User passenger = userRepository.findById(request.passengerId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.passengerId()));

        if (ride.getDriverID() != null && ride.getDriverID().getId().equals(passenger.getId())) {
            throw new IllegalArgumentException("Driver cannot request to join their own ride");
        }

        if (rideRequestRepository.existsByRideAndPassenger(ride, passenger)) {
            throw new RideRequestAlreadyExistsException("A ride request already exists for this ride and passenger");
        }

        if (ride.getAvailableSeats() <= 0) {
            throw new IllegalArgumentException("No available seats for this ride");
        }

        RideRequest rideRequest = new RideRequest();
        rideRequest.setRide(ride);
        rideRequest.setPassenger(passenger);
        rideRequest.setStatus("PENDING");

        RideRequest saved = rideRequestRepository.save(rideRequest);
        return mapToRideRequestResponse(saved);
    }

    public RideRequestResponse getRideRequest(UUID id) {
        RideRequest rideRequest = rideRequestRepository.findById(id)
                .orElseThrow(() -> new RideRequestNotFoundException("Ride request not found with id: " + id));
        return mapToRideRequestResponse(rideRequest);
    }

    public List<RideRequestResponse> getAllRideRequests(UUID rideId, UUID passengerId) {
        if (rideId != null) {
            return rideRequestRepository.findByRide_Id(rideId).stream()
                    .map(this::mapToRideRequestResponse)
                    .toList();
        }
        if (passengerId != null) {
            return rideRequestRepository.findByPassenger_Id(passengerId).stream()
                    .map(this::mapToRideRequestResponse)
                    .toList();
        }
        return rideRequestRepository.findAll().stream()
                .map(this::mapToRideRequestResponse)
                .toList();
    }

    public RideRequestResponse modifyRideRequest(UUID id, UpdateRideRequestDto request) {
        RideRequest rideRequest = rideRequestRepository.findById(id)
                .orElseThrow(() -> new RideRequestNotFoundException("Ride request not found with id: " + id));

        String newStatus = request.status().toUpperCase();
        String currentStatus = rideRequest.getStatus();

        if (!currentStatus.equalsIgnoreCase(newStatus)) {
            Ride ride = rideRequest.getRide();

            if ("ACCEPTED".equalsIgnoreCase(newStatus)) {
                if (ride != null) {
                    if (ride.getAvailableSeats() <= 0) {
                        throw new IllegalArgumentException("No available seats left to accept this request");
                    }
                    ride.setAvailableSeats(ride.getAvailableSeats() - 1);
                    rideRepository.save(ride);
                }
            } else if ("ACCEPTED".equalsIgnoreCase(currentStatus) &&
                    ("CANCELLED".equalsIgnoreCase(newStatus) || "REJECTED".equalsIgnoreCase(newStatus))) {
                if (ride != null) {
                    ride.setAvailableSeats(ride.getAvailableSeats() + 1);
                    rideRepository.save(ride);
                }
            }

            rideRequest.setStatus(newStatus);
        }

        RideRequest updated = rideRequestRepository.save(rideRequest);
        return mapToRideRequestResponse(updated);
    }

    public RideRequestResponse mapToRideRequestResponse(RideRequest request) {
        UUID rideId = request.getRide() != null ? request.getRide().getId() : null;
        UUID passengerId = request.getPassenger() != null ? request.getPassenger().getId() : null;
        return new RideRequestResponse(
                request.getId(),
                rideId,
                passengerId,
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}

