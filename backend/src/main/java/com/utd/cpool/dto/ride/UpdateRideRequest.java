package com.utd.cpool.dto.ride;

import java.time.LocalDateTime;

public record UpdateRideRequest(
    String rideId,
    String status,
    LocalDateTime departureTime,
    int availableSeats
){}
    
