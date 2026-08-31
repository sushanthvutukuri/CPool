package com.utd.cpool.repository;

import com.utd.cpool.entity.Ride;
import com.utd.cpool.entity.RideRequest;
import com.utd.cpool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, UUID> {
    List<RideRequest> findByRide(Ride ride);
    List<RideRequest> findByPassenger(User passenger);
    List<RideRequest> findByRide_Id(UUID rideId);
    List<RideRequest> findByPassenger_Id(UUID passengerId);
    boolean existsByRideAndPassenger(Ride ride, User passenger);
}

