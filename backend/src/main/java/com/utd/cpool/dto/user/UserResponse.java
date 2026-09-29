package com.utd.cpool.dto.user;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String phoneNumber
) {
    public UserResponse(UUID id, String name, String email) {
        this(id, name, email, "");
    }
}