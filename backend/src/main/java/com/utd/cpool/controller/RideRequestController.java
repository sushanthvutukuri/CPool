package com.utd.cpool.controller;

import com.utd.cpool.dto.riderequest.CreateRideRequestDto;
import com.utd.cpool.dto.riderequest.RideRequestResponse;
import com.utd.cpool.dto.riderequest.UpdateRideRequestDto;
import com.utd.cpool.service.RideRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ride-requests")
public class RideRequestController {

    private final RideRequestService rideRequestService;

    public RideRequestController(RideRequestService rideRequestService) {
        this.rideRequestService = rideRequestService;
    }

    @PostMapping({"", "/create"})
    public RideRequestResponse createRideRequest(@Valid @RequestBody CreateRideRequestDto request) {
        return rideRequestService.createRideRequest(request);
    }

    @GetMapping("/{id}")
    public RideRequestResponse getRideRequest(@PathVariable UUID id) {
        return rideRequestService.getRideRequest(id);
    }

    @GetMapping
    public List<RideRequestResponse> getAllRideRequests(
            @RequestParam(required = false) UUID rideId,
            @RequestParam(required = false) UUID passengerId) {
        return rideRequestService.getAllRideRequests(rideId, passengerId);
    }

    @PutMapping("/{id}")
    public RideRequestResponse modifyRideRequest(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRideRequestDto request) {
        return rideRequestService.modifyRideRequest(id, request);
    }

    @PatchMapping("/{id}")
    public RideRequestResponse patchRideRequest(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRideRequestDto request) {
        return rideRequestService.modifyRideRequest(id, request);
    }
}

