package com.utd.cpool.controller;

import com.utd.cpool.dto.ride.CreateRideRequest;
import com.utd.cpool.dto.ride.RideResponse;
import com.utd.cpool.dto.ride.UpdateRideRequest;
import com.utd.cpool.dto.riderequest.UpdateRideRequestDto;
import com.utd.cpool.service.RideService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping({"", "/create"})
    public List<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        return rideService.createRide(request);
    }

    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable UUID id) {
        return rideService.getRide(id);
    }

    @GetMapping()
    public List<RideResponse> getAllRides() {
        return rideService.getAllRides();
    }

    @PostMapping("/update")
    public RideResponse updateRide(@RequestBody UpdateRideRequest request){
        return rideService.updateRide(request);
    }
}
