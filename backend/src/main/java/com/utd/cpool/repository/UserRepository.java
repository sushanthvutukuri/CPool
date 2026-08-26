package com.utd.cpool.repository;

import com.utd.cpool.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {
    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);
}
