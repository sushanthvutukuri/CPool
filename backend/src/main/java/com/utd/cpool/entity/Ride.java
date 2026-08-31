package com.utd.cpool.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name="rides")
public class Ride{
    public enum RideStatus{
        OPEN,
        FULL,
        CANCELED,
        COMPLETED
    }
    @Id
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="driver_id", nullable=false)
    private User driverID;

    private String origin;

    private String destination;

    @Column(name="departure_time")
    private LocalDateTime departureTime;

    @Column(name="available_seats")
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="recurring")
    private boolean recurring;

    @PrePersist
    protected void onCreate()
    {
        if(this.id == null) {
            this.id = UUID.randomUUID();
        }
        status=RideStatus.OPEN;
        createdAt=LocalDateTime.now();
        recurring=false;
    }

    public Ride() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getDriverID() {
        return driverID;
    }

    public void setDriverID(User driiverID) {
        this.driverID = driiverID;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
}