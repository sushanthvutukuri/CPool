package com.utd.cpool.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

import com.utd.cpool.dto.auth.AuthResponse;
import com.utd.cpool.dto.auth.LoginRequest;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.UserRepository;
import com.utd.cpool.exception.InvalidCredentials;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public AuthResponse authenticateUser(LoginRequest request)
    {
        Optional<User> user=userRepository.findByEmail(request.email());
        if (user.isEmpty())
        {
            throw new UserNotFoundException("User do not exist");
        }
        String passwordHash=user.get().getPassword();
        if(passwordEncoder.matches(request.password(), passwordHash)!=true)
        {
            throw new InvalidCredentials("Email or password is incorrect");
        }

        User authenticatedUser = user.get();
        return new AuthResponse("Success", authenticatedUser.getId(), authenticatedUser.getName(), authenticatedUser.getEmail());

    }

}
