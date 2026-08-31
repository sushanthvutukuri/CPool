package com.utd.cpool.dto.riderequest;

import jakarta.validation.constraints.NotBlank;

public record UpdateRideRequestDto(
    @NotBlank(message = "Status cannot be blank")
    String status
) {}

