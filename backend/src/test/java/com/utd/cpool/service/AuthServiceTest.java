package com.utd.cpool.service;

import com.utd.cpool.dto.auth.AuthResponse;
import com.utd.cpool.dto.auth.LoginRequest;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.InvalidCredentials;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void testAuthenticateUserSuccess() {
        LoginRequest request = new LoginRequest("user@utdallas.edu", "Password123!");
        User user = new User();
        user.setEmail("user@utdallas.edu");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("user@utdallas.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashedPassword")).thenReturn(true);

        AuthResponse response = authService.authenticateUser(request);

        assertEquals("Success", response.status());
    }

    @Test
    void testAuthenticateUserNotFound() {
        LoginRequest request = new LoginRequest("nonexistent@utdallas.edu", "Password123!");

        when(userRepository.findByEmail("nonexistent@utdallas.edu")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.authenticateUser(request));
    }

    @Test
    void testAuthenticateUserInvalidPassword() {
        LoginRequest request = new LoginRequest("user@utdallas.edu", "WrongPassword");
        User user = new User();
        user.setEmail("user@utdallas.edu");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("user@utdallas.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(InvalidCredentials.class, () -> authService.authenticateUser(request));
    }
}

