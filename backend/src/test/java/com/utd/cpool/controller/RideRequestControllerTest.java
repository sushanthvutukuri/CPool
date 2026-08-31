package com.utd.cpool.controller;

import com.utd.cpool.dto.riderequest.CreateRideRequestDto;
import com.utd.cpool.dto.riderequest.RideRequestResponse;
import com.utd.cpool.dto.riderequest.UpdateRideRequestDto;
import com.utd.cpool.exception.GlobalExceptionHandler;
import com.utd.cpool.exception.RideRequestNotFoundException;
import com.utd.cpool.service.RideRequestService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RideRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RideRequestService rideRequestService;

    @InjectMocks
    private RideRequestController rideRequestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rideRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateRideRequestSuccess() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        RideRequestResponse response = new RideRequestResponse(
                requestId,
                rideId,
                passengerId,
                "PENDING",
                LocalDateTime.now()
        );

        when(rideRequestService.createRideRequest(any(CreateRideRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "rideId": "%s",
                    "passengerId": "%s"
                }
                """.formatted(rideId, passengerId);

        mockMvc.perform(post("/api/ride-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()))
                .andExpect(jsonPath("$.rideId").value(rideId.toString()))
                .andExpect(jsonPath("$.passengerId").value(passengerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetRideRequestByIdSuccess() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        RideRequestResponse response = new RideRequestResponse(
                requestId,
                rideId,
                passengerId,
                "PENDING",
                LocalDateTime.now()
        );

        when(rideRequestService.getRideRequest(requestId)).thenReturn(response);

        mockMvc.perform(get("/api/ride-requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()))
                .andExpect(jsonPath("$.rideId").value(rideId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetRideRequestByIdNotFound() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(rideRequestService.getRideRequest(requestId))
                .thenThrow(new RideRequestNotFoundException("Ride request not found with id: " + requestId));

        mockMvc.perform(get("/api/ride-requests/{id}", requestId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ride request not found with id: " + requestId));
    }

    @Test
    void testGetAllRideRequests() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        List<RideRequestResponse> list = List.of(
                new RideRequestResponse(requestId, rideId, passengerId, "PENDING", LocalDateTime.now())
        );

        when(rideRequestService.getAllRideRequests(null, null)).thenReturn(list);

        mockMvc.perform(get("/api/ride-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(requestId.toString()));
    }

    @Test
    void testModifyRideRequestSuccess() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();

        RideRequestResponse response = new RideRequestResponse(
                requestId,
                rideId,
                passengerId,
                "ACCEPTED",
                LocalDateTime.now()
        );

        when(rideRequestService.modifyRideRequest(eq(requestId), any(UpdateRideRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "status": "ACCEPTED"
                }
                """;

        mockMvc.perform(put("/api/ride-requests/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}

