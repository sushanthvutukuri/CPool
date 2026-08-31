package com.utd.cpool.service;

import com.utd.cpool.dto.ride.CreateRideRequest;
import com.utd.cpool.dto.ride.RideResponse;
import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.RideRepository;
import com.utd.cpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    private RideService rideService;

    @BeforeEach
    void setUp() {
        rideService = new RideService(rideRepository, userRepository);
    }

    @Test
    void testParseDepartureTimeFormats() {
        assertEquals(LocalDateTime.of(2026, 1, 18, 20, 0), rideService.parseDepartureTime("January 18, 2026 8:00PM"));
        assertEquals(LocalDateTime.of(2026, 1, 18, 20, 0), rideService.parseDepartureTime("January 18, 2026 8:00 PM"));
        assertEquals(LocalDateTime.of(2026, 1, 18, 20, 0), rideService.parseDepartureTime("Jan 18, 2026 08:00pm"));
        assertEquals(LocalDateTime.of(2026, 1, 18, 20, 0), rideService.parseDepartureTime("2026-01-18T20:00:00"));
    }

    @Test
    void testCreateSingleRide() {
        UUID driverId = UUID.randomUUID();
        User driver = new User();
        driver.setId(driverId);

        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRideRequest request = new CreateRideRequest(
                driverId,
                "UTD North Campus",
                "DFW Airport",
                "January 18, 2026 8:00PM",
                4,
                false
        );

        List<RideResponse> responses = rideService.createRide(request);

        assertEquals(1, responses.size());
        RideResponse ride = responses.get(0);
        assertEquals(driverId, ride.driverId());
        assertEquals("UTD North Campus", ride.origin());
        assertEquals("DFW Airport", ride.destination());
        assertEquals(LocalDateTime.of(2026, 1, 18, 20, 0), ride.departureTime());
        assertEquals(4, ride.availableSeats());
        assertFalse(ride.recurring());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ride>> captor = ArgumentCaptor.forClass(List.class);
        verify(rideRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void testCreateRecurringRides() {
        UUID driverId = UUID.randomUUID();
        User driver = new User();
        driver.setId(driverId);

        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRideRequest request = new CreateRideRequest(
                driverId,
                "UTD North Campus",
                "Love Field Airport",
                "January 18, 2026 8:00PM",
                3,
                true
        );

        List<RideResponse> responses = rideService.createRide(request);

        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 18, 20, 0);
        assertEquals(5, responses.size()); // Base ride + 4 weekly recurring rides
        assertEquals(baseTime, responses.get(0).departureTime());
        assertEquals(baseTime.plusWeeks(1), responses.get(1).departureTime());
        assertEquals(baseTime.plusWeeks(2), responses.get(2).departureTime());
        assertEquals(baseTime.plusWeeks(3), responses.get(3).departureTime());
        assertEquals(baseTime.plusWeeks(4), responses.get(4).departureTime());
        assertTrue(responses.get(0).recurring());
        assertTrue(responses.get(1).recurring());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ride>> captor = ArgumentCaptor.forClass(List.class);
        verify(rideRepository).saveAll(captor.capture());
        assertEquals(5, captor.getValue().size());
    }

    @Test
    void testCreateRideDriverNotFound() {
        UUID driverId = UUID.randomUUID();
        when(userRepository.findById(driverId)).thenReturn(Optional.empty());

        CreateRideRequest request = new CreateRideRequest(
                driverId,
                "Origin",
                "Destination",
                "January 18, 2026 8:00PM",
                2,
                false
        );

        assertThrows(UserNotFoundException.class, () -> rideService.createRide(request));
        verify(rideRepository, never()).saveAll(any());
    }

    @Test
    void testCreateRideInvalidDateFormat() {
        UUID driverId = UUID.randomUUID();
        User driver = new User();
        driver.setId(driverId);

        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));

        CreateRideRequest request = new CreateRideRequest(
                driverId,
                "Origin",
                "Destination",
                "not-a-valid-date",
                2,
                false
        );

        assertThrows(IllegalArgumentException.class, () -> rideService.createRide(request));
    }

    @Test
    void testCreateRideAlreadyExists() {
        UUID driverId = UUID.randomUUID();
        User driver = new User();
        driver.setId(driverId);

        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.existsByDriverIDAndDepartureTime(eq(driver), any(LocalDateTime.class))).thenReturn(true);

        CreateRideRequest request = new CreateRideRequest(
                driverId,
                "Origin",
                "Destination",
                "January 18, 2026 8:00PM",
                2,
                false
        );

        assertThrows(com.utd.cpool.exception.RideAlreadyExistsException.class, () -> rideService.createRide(request));
        verify(rideRepository, never()).saveAll(any());
    }
}
