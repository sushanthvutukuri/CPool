package com.utd.cpool.service;

import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.UserAlreadyExistsException;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testCreateUserSuccess() {
        CreateUserRequest request = new CreateUserRequest("john@utdallas.edu", "John Doe", "Password123!");

        when(userRepository.existsByEmail("john@utdallas.edu")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response.id());
        assertEquals("John Doe", response.name());
        assertEquals("john@utdallas.edu", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("existing@utdallas.edu", "Existing User", "Password123!");

        when(userRepository.existsByEmail("existing@utdallas.edu")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUserSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setName("Jane Doe");
        user.setEmail("jane@utdallas.edu");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(userId);

        assertEquals(userId, response.id());
        assertEquals("Jane Doe", response.name());
        assertEquals("jane@utdallas.edu", response.email());
    }

    @Test
    void testGetUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));
    }
}

