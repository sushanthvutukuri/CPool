package com.utd.cpool.service;

import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.entity.User;
import com.utd.cpool.repository.UserRepository;
import com.utd.cpool.config.*;
import com.utd.cpool.exception.UserNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.PasswordAuthentication;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncrypter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncrypter=passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request){
        String passwordHash=passwordEncrypter.encode(request.password());
        User user=new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordHash);
        User savedUser=userRepository.save(user);
        return mapToUserReponse(savedUser);
    }

    public UserResponse getUser(UUID id)
    {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found with id: " + id));
        return mapToUserReponse(user);
    }

    public UserResponse mapToUserReponse(User user) {
        return new UserResponse(user.getId(),user.getName(),user.getEmail());
    }
}
