package com.utd.cpool.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message="Email cannot be blank") 
    @Email(message="Invalid email format")
    String email, 


    @NotBlank(message="Name cannot be blank")
    @Size(min=2, max=50, message="Name must be between 2 and 50 characters")
    String name,
    
    
    @NotBlank(message="Password cannot be blank")
    @Size(message="Password must be between 8 and 64 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    String password
){}