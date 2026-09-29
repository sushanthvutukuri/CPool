package com.utd.cpool.dto.auth;

import java.util.UUID;

public record AuthResponse(
    String status,
    UUID userId,
    String name,
    String email,
    String phoneNumber
) {
    public AuthResponse(String status) {
        this(status, null, null, null, null);
    }

    public AuthResponse(String status, UUID userId, String name, String email) {
        this(status, userId, name, email, null);
    }
}