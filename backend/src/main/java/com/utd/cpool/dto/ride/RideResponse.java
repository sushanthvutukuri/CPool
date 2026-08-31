package com.utd.cpool.dto.ride;

import java.time.LocalDateTime;
import java.util.UUID;

import com.utd.cpool.entity.Ride.RideStatus;

public record RideResponse(
    UUID id,
    UUID driverId,
    String origin,
    String destination,
    LocalDateTime departureTime,
    int availableSeats,
    RideStatus status,
    LocalDateTime createdAt,
    boolean recurring
) {
}
