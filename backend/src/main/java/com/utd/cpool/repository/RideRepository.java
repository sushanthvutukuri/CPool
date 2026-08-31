package com.utd.cpool.repository;

import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {
    List<Ride> findByDriverID(User driverID);
    List<Ride> findByRecurring(boolean recurring);
    boolean existsByDriverIDAndDepartureTime(User driverID, LocalDateTime departureTime);
}

