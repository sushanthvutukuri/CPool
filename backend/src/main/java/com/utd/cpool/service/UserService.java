package com.utd.cpool.service;

import com.utd.cpool.dto.user.CreateUserRequest;
import com.utd.cpool.dto.user.UserResponse;
import com.utd.cpool.entity.User;
import com.utd.cpool.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request){
        User user=new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setId(UUID.randomUUID());
        User savedUser=userRepository.save(user);
        return new UserResponse(savedUser.getId(),savedUser.getName(),savedUser.getEmail());
    }
}
