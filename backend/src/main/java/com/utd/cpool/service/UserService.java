package com.utd.cpool.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.entity.User;
import com.utd.cpool.exception.UserAlreadyExistsException;
import com.utd.cpool.exception.UserNotFoundException;
import com.utd.cpool.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request){
        if (userRepository.existsByEmail(request.email()))
        {
            throw new UserAlreadyExistsException("Email already in use "+request.email());
        }
        String passwordHash=passwordEncoder.encode(request.password());
        User user=new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordHash);
        user.setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber() : "");
        User savedUser=userRepository.save(user);
        return mapToUserReponse(savedUser);
    }

    public UserResponse getUser(UUID id)
    {
        User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found with id: " + id));
        return mapToUserReponse(user);
    }

    public UserResponse mapToUserReponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber());
    }
}
