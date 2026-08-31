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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideRequestServiceTest {

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    private RideRequestService rideRequestService;

    @BeforeEach
    void setUp() {
        rideRequestService = new RideRequestService(rideRequestRepository, rideRepository, userRepository);
    }

    @Test
    void testCreateRideRequestSuccess() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        User driver = new User();
        driver.setId(driverId);

        User passenger = new User();
        passenger.setId(passengerId);

        Ride ride = new Ride();
        ride.setId(rideId);
        ride.setDriverID(driver);
        ride.setAvailableSeats(3);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRequestRepository.existsByRideAndPassenger(ride, passenger)).thenReturn(false);
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> {
            RideRequest req = inv.getArgument(0);
            req.setId(UUID.randomUUID());
            return req;
        });

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, passengerId);
        RideRequestResponse response = rideRequestService.createRideRequest(dto);

        assertNotNull(response.id());
        assertEquals(rideId, response.rideId());
        assertEquals(passengerId, response.passengerId());
        assertEquals("PENDING", response.status());
    }

    @Test
    void testCreateRideRequestRideNotFound() {
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, passengerId);
        assertThrows(RideNotFoundException.class, () -> rideRequestService.createRideRequest(dto));
    }

    @Test
    void testCreateRideRequestPassengerNotFound() {
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        Ride ride = new Ride();
        ride.setId(rideId);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.empty());

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, passengerId);
        assertThrows(UserNotFoundException.class, () -> rideRequestService.createRideRequest(dto));
    }

    @Test
    void testCreateRideRequestDriverCannotRequestOwnRide() {
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User driver = new User();
        driver.setId(userId);

        Ride ride = new Ride();
        ride.setId(rideId);
        ride.setDriverID(driver);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(userId)).thenReturn(Optional.of(driver));

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, userId);
        assertThrows(IllegalArgumentException.class, () -> rideRequestService.createRideRequest(dto));
    }

    @Test
    void testCreateRideRequestDuplicate() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        User driver = new User();
        driver.setId(driverId);

        User passenger = new User();
        passenger.setId(passengerId);

        Ride ride = new Ride();
        ride.setId(rideId);
        ride.setDriverID(driver);
        ride.setAvailableSeats(2);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRequestRepository.existsByRideAndPassenger(ride, passenger)).thenReturn(true);

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, passengerId);
        assertThrows(RideRequestAlreadyExistsException.class, () -> rideRequestService.createRideRequest(dto));
    }

    @Test
    void testCreateRideRequestNoSeats() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        User driver = new User();
        driver.setId(driverId);

        User passenger = new User();
        passenger.setId(passengerId);

        Ride ride = new Ride();
        ride.setId(rideId);
        ride.setDriverID(driver);
        ride.setAvailableSeats(0);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRequestRepository.existsByRideAndPassenger(ride, passenger)).thenReturn(false);

        CreateRideRequestDto dto = new CreateRideRequestDto(rideId, passengerId);
        assertThrows(IllegalArgumentException.class, () -> rideRequestService.createRideRequest(dto));
    }

    @Test
    void testGetRideRequestSuccess() {
        UUID requestId = UUID.randomUUID();
        RideRequest req = new RideRequest();
        req.setId(requestId);
        req.setStatus("PENDING");

        when(rideRequestRepository.findById(requestId)).thenReturn(Optional.of(req));

        RideRequestResponse response = rideRequestService.getRideRequest(requestId);
        assertEquals(requestId, response.id());
        assertEquals("PENDING", response.status());
    }

    @Test
    void testGetRideRequestNotFound() {
        UUID requestId = UUID.randomUUID();
        when(rideRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(RideRequestNotFoundException.class, () -> rideRequestService.getRideRequest(requestId));
    }

    @Test
    void testModifyRideRequestToAcceptedDecrementsSeats() {
        UUID requestId = UUID.randomUUID();
        Ride ride = new Ride();
        ride.setId(UUID.randomUUID());
        ride.setAvailableSeats(3);

        RideRequest req = new RideRequest();
        req.setId(requestId);
        req.setRide(ride);
        req.setStatus("PENDING");

        when(rideRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RideRequestResponse response = rideRequestService.modifyRideRequest(requestId, new UpdateRideRequestDto("ACCEPTED"));

        assertEquals("ACCEPTED", response.status());
        assertEquals(2, ride.getAvailableSeats());
        verify(rideRepository).save(ride);
    }

    @Test
    void testModifyRideRequestToCancelledRestoresSeats() {
        UUID requestId = UUID.randomUUID();
        Ride ride = new Ride();
        ride.setId(UUID.randomUUID());
        ride.setAvailableSeats(2);

        RideRequest req = new RideRequest();
        req.setId(requestId);
        req.setRide(ride);
        req.setStatus("ACCEPTED");

        when(rideRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RideRequestResponse response = rideRequestService.modifyRideRequest(requestId, new UpdateRideRequestDto("CANCELLED"));

        assertEquals("CANCELLED", response.status());
        assertEquals(3, ride.getAvailableSeats());
        verify(rideRepository).save(ride);
    }
}

