package com.utd.cpool.integration;

import com.utd.cpool.dto.auth.AuthResponse;
import com.utd.cpool.dto.auth.LoginRequest;
import com.utd.cpool.dto.ride.CreateRideRequest;
import com.utd.cpool.dto.ride.RideResponse;
import com.utd.cpool.dto.riderequest.CreateRideRequestDto;
import com.utd.cpool.dto.riderequest.RideRequestResponse;
import com.utd.cpool.dto.riderequest.UpdateRideRequestDto;
import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.RideRequest;
import com.utd.cpool.entity.User;
import com.utd.cpool.repository.RideRepository;
import com.utd.cpool.repository.RideRequestRepository;
import com.utd.cpool.repository.UserRepository;
import com.utd.cpool.service.AuthService;
import com.utd.cpool.service.RideRequestService;
import com.utd.cpool.service.RideService;
import com.utd.cpool.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiDatabaseIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideRequestRepository rideRequestRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private AuthService authService;
    private RideService rideService;
    private RideRequestService rideRequestService;

    // Track all created test entity IDs for post-test database cleanup
    private final List<UUID> createdUserIds = new ArrayList<>();
    private final List<UUID> createdRideIds = new ArrayList<>();
    private final List<UUID> createdRideRequestIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
        authService = new AuthService(userRepository, passwordEncoder);
        rideService = new RideService(rideRepository, userRepository);
        rideRequestService = new RideRequestService(rideRequestRepository, rideRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        // Guaranteed cleanup of any created database additions post-testing in reverse dependency order
        try {
            for (UUID reqId : createdRideRequestIds) {
                rideRequestRepository.deleteById(reqId);
            }
            for (UUID rideId : createdRideIds) {
                rideRepository.deleteById(rideId);
            }
            for (UUID userId : createdUserIds) {
                userRepository.deleteById(userId);
            }
        } finally {
            createdRideRequestIds.clear();
            createdRideIds.clear();
            createdUserIds.clear();
        }
    }

    @Test
    void testEndToEndUserCreationAuthenticationRideSchedulingAndRideRequestLifecycle() {
        // 1. Create Driver User
        UUID driverId = UUID.randomUUID();
        CreateUserRequest driverRequest = new CreateUserRequest("driver@utdallas.edu", "Driver Dave", "Password123!");

        when(userRepository.existsByEmail("driver@utdallas.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(driverId);
            createdUserIds.add(driverId);
            return u;
        });

        UserResponse driverResponse = userService.createUser(driverRequest);
        assertNotNull(driverResponse.id());
        assertEquals("driver@utdallas.edu", driverResponse.email());

        // 2. Create Passenger User
        UUID passengerId = UUID.randomUUID();
        User passenger = new User();
        passenger.setId(passengerId);
        passenger.setName("Passenger Pam");
        passenger.setEmail("passenger@utdallas.edu");
        createdUserIds.add(passengerId);

        // 3. Authenticate Driver
        User savedDriver = new User();
        savedDriver.setId(driverId);
        savedDriver.setName("Driver Dave");
        savedDriver.setEmail("driver@utdallas.edu");
        savedDriver.setPassword(passwordEncoder.encode("Password123!"));

        when(userRepository.findByEmail("driver@utdallas.edu")).thenReturn(Optional.of(savedDriver));
        AuthResponse authResponse = authService.authenticateUser(new LoginRequest("driver@utdallas.edu", "Password123!"));
        assertEquals("Success", authResponse.status());

        // 4. Create Recurring Rides for next month
        when(userRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(rideRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Ride> list = inv.getArgument(0);
            for (Ride r : list) {
                if (r.getId() == null) r.setId(UUID.randomUUID());
                createdRideIds.add(r.getId());
            }
            return list;
        });

        CreateRideRequest rideRequest = new CreateRideRequest(
                driverId,
                "UTD Student Union",
                "Dallas Love Field",
                "January 18, 2026 8:00PM",
                3,
                true
        );

        List<RideResponse> rides = rideService.createRide(rideRequest);
        assertEquals(5, rides.size()); // 1 base ride + 4 weekly recurring rides
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 18, 20, 0);
        assertEquals(baseTime, rides.get(0).departureTime());

        // 5. Passenger Requests to Join Ride
        UUID initialRideId = rides.get(0).id();
        Ride targetRide = new Ride();
        targetRide.setId(initialRideId);
        targetRide.setDriverID(savedDriver);
        targetRide.setAvailableSeats(3);

        when(rideRepository.findById(initialRideId)).thenReturn(Optional.of(targetRide));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRequestRepository.existsByRideAndPassenger(targetRide, passenger)).thenReturn(false);

        UUID requestId = UUID.randomUUID();
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> {
            RideRequest req = inv.getArgument(0);
            if (req.getId() == null) req.setId(requestId);
            createdRideRequestIds.add(req.getId());
            return req;
        });

        RideRequestResponse requestResponse = rideRequestService.createRideRequest(new CreateRideRequestDto(initialRideId, passengerId));
        assertNotNull(requestResponse.id());
        assertEquals("PENDING", requestResponse.status());

        // 6. Driver Accepts Request -> Decrements Available Seats
        RideRequest pendingRequest = new RideRequest();
        pendingRequest.setId(requestId);
        pendingRequest.setRide(targetRide);
        pendingRequest.setPassenger(passenger);
        pendingRequest.setStatus("PENDING");

        when(rideRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        RideRequestResponse acceptedResponse = rideRequestService.modifyRideRequest(requestId, new UpdateRideRequestDto("ACCEPTED"));
        assertEquals("ACCEPTED", acceptedResponse.status());
        assertEquals(2, targetRide.getAvailableSeats());
    }
}
