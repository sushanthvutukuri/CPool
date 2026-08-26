package com.utd.cpool.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utd.cpool.dto.auth.AuthResponse;
import com.utd.cpool.dto.auth.LoginRequest;
import com.utd.cpool.service.AuthService;
import com.utd.cpool.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService){
        this.userService=userService;
        this.authService=authService;

    }

    @PostMapping("/authenticate-user")
    public AuthResponse userAuthentication(@Valid @RequestBody LoginRequest request)
    {
        return authService.authenticateUser(request);
    }    
}
