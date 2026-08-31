package com.utd.cpool.controller;

import com.utd.cpool.dto.ride.CreateRideRequest;
import com.utd.cpool.dto.ride.RideResponse;
import com.utd.cpool.entity.Ride.RideStatus;
import com.utd.cpool.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController)
                .setControllerAdvice(new com.utd.cpool.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateSingleRideSuccess() throws Exception {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = LocalDateTime.of(2026, 1, 18, 20, 0);

        RideResponse response = new RideResponse(
                rideId,
                driverId,
                "UTD Campus",
                "DFW Airport",
                departureTime,
                3,
                RideStatus.OPEN,
                now,
                false
        );

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(List.of(response));

        String requestJson = """
                {
                    "driverId": "%s",
                    "origin": "UTD Campus",
                    "destination": "DFW Airport",
                    "departureTime": "January 18, 2026 8:00PM",
                    "availableSeats": 3,
                    "recurring": false
                }
                """.formatted(driverId);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(rideId.toString()))
                .andExpect(jsonPath("$[0].driverId").value(driverId.toString()))
                .andExpect(jsonPath("$[0].origin").value("UTD Campus"))
                .andExpect(jsonPath("$[0].destination").value("DFW Airport"))
                .andExpect(jsonPath("$[0].departureTime").value("2026-01-18T20:00:00"))
                .andExpect(jsonPath("$[0].availableSeats").value(3))
                .andExpect(jsonPath("$[0].recurring").value(false))
                .andExpect(jsonPath("$[0].status").value("Open"));
    }

    @Test
    void testCreateRecurringRidesSuccess() throws Exception {
        UUID driverId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = LocalDateTime.of(2026, 1, 18, 20, 0);

        List<RideResponse> recurringResponses = List.of(
                new RideResponse(UUID.randomUUID(), driverId, "UTD Campus", "DFW Airport", departureTime, 3, RideStatus.OPEN, now, true),
                new RideResponse(UUID.randomUUID(), driverId, "UTD Campus", "DFW Airport", departureTime.plusWeeks(1), 3, RideStatus.OPEN, now, true),
                new RideResponse(UUID.randomUUID(), driverId, "UTD Campus", "DFW Airport", departureTime.plusWeeks(2), 3, RideStatus.OPEN, now, true),
                new RideResponse(UUID.randomUUID(), driverId, "UTD Campus", "DFW Airport", departureTime.plusWeeks(3), 3, RideStatus.OPEN, now, true),
                new RideResponse(UUID.randomUUID(), driverId, "UTD Campus", "DFW Airport", departureTime.plusWeeks(4), 3, RideStatus.OPEN, now, true)
        );

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(recurringResponses);

        String requestJson = """
                {
                    "driverId": "%s",
                    "origin": "UTD Campus",
                    "destination": "DFW Airport",
                    "departureTime": "January 18, 2026 8:00PM",
                    "availableSeats": 3,
                    "recurring": true
                }
                """.formatted(driverId);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].departureTime").value("2026-01-18T20:00:00"))
                .andExpect(jsonPath("$[1].departureTime").value("2026-01-25T20:00:00"))
                .andExpect(jsonPath("$[2].departureTime").value("2026-02-01T20:00:00"))
                .andExpect(jsonPath("$[3].departureTime").value("2026-02-08T20:00:00"))
                .andExpect(jsonPath("$[4].departureTime").value("2026-02-15T20:00:00"))
                .andExpect(jsonPath("$[0].recurring").value(true));
    }

    @Test
    void testGetRideById() throws Exception {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDateTime departureTime = LocalDateTime.of(2026, 1, 18, 20, 0);

        RideResponse response = new RideResponse(
                rideId,
                driverId,
                "UTD Campus",
                "Dallas Downtown",
                departureTime,
                2,
                RideStatus.OPEN,
                LocalDateTime.now(),
                false
        );

        when(rideService.getRide(rideId)).thenReturn(response);

        mockMvc.perform(get("/api/rides/{id}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rideId.toString()))
                .andExpect(jsonPath("$.origin").value("UTD Campus"))
                .andExpect(jsonPath("$.destination").value("Dallas Downtown"));
    }

    @Test
    void testCreateRideValidationFailure() throws Exception {
        String invalidJson = """
                {
                    "driverId": null,
                    "origin": "",
                    "destination": "",
                    "departureTime": "",
                    "availableSeats": 0
                }
                """;

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.origin").exists())
                .andExpect(jsonPath("$.errors.destination").exists())
                .andExpect(jsonPath("$.errors.availableSeats").exists());
    }

    @Test
    void testCreateRideAlreadyExists() throws Exception {
        UUID driverId = UUID.randomUUID();
        when(rideService.createRide(any(CreateRideRequest.class)))
                .thenThrow(new com.utd.cpool.exception.RideAlreadyExistsException("A ride already exists for this driver at 2026-01-18T20:00"));

        String requestJson = """
                {
                    "driverId": "%s",
                    "origin": "UTD Campus",
                    "destination": "DFW Airport",
                    "departureTime": "January 18, 2026 8:00PM",
                    "availableSeats": 3,
                    "recurring": false
                }
                """.formatted(driverId);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("A ride already exists for this driver at 2026-01-18T20:00"));
    }
}
