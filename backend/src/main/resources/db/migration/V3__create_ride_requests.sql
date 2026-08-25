CREATE TABLE ride_requests (
    id UUID PRIMARY KEY,
    ride_id UUID NOT NULL,
    passenger_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_request_ride
        FOREIGN KEY (ride_id)
        REFERENCES rides(id),

    CONSTRAINT fk_request_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES users(id)
);