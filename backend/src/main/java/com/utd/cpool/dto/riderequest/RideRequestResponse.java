package com.utd.cpool.dto.riderequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record RideRequestResponse(
    UUID id,
    UUID rideId,
    UUID passengerId,
    String status,
    LocalDateTime createdAt
) {}

