package com.utd.cpool.dto.ride;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRideRequest(
    @NotNull(message = "Driver ID cannot be null")
    UUID driverId,

    @NotBlank(message = "Origin cannot be blank")
    String origin,

    @NotBlank(message = "Destination cannot be blank")
    String destination,

    @NotBlank(message = "Departure time cannot be blank")
    String departureTime,

    @Min(value = 1, message = "Available seats must be at least 1")
    @Max(value = 10, message = "Available seats cannot be greater than 10")
    int availableSeats,

    Boolean recurring
) {
    public CreateRideRequest {
        if (recurring == null) {
            recurring = false;
        }
    }
}
