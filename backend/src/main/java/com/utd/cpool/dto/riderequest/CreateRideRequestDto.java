package com.utd.cpool.dto.riderequest;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRideRequestDto(
    @NotNull(message = "Ride ID cannot be null")
    UUID rideId,

    @NotNull(message = "Passenger ID cannot be null")
    UUID passengerId
) {}

