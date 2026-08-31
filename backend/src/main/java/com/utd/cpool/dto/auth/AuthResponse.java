package com.utd.cpool.dto.auth;

import java.util.UUID;

public record AuthResponse(
    String status,
    UUID userId,
    String name,
    String email
) {
    public AuthResponse(String status) {
        this(status, null, null, null);
    }
}