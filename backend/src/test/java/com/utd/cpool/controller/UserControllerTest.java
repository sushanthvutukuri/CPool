package com.utd.cpool.controller;

import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.exception.GlobalExceptionHandler;
import com.utd.cpool.exception.UserAlreadyExistsException;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(userId, "John Doe", "john.doe@utdallas.edu");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        String requestJson = """
                {
                    "name": "John Doe",
                    "email": "john.doe@utdallas.edu",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@utdallas.edu"));
    }

    @Test
    void testCreateUserEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already in use john.doe@utdallas.edu"));

        String requestJson = """
                {
                    "name": "John Doe",
                    "email": "john.doe@utdallas.edu",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Email already in use john.doe@utdallas.edu"));
    }

    @Test
    void testCreateUserValidationFailure() throws Exception {
        // Name blank, email invalid, password weak
        String invalidJson = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void testGetUserByIdSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(userId, "Jane Doe", "jane.doe@utdallas.edu");

        when(userService.getUser(userId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@utdallas.edu"));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.getUser(userId))
                .thenThrow(new UserNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + userId));
    }
}

