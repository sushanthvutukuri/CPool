CREATE TABLE rides (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    available_seats INTEGER NOT NULL,

    CONSTRAINT fk_ride_driver
        FOREIGN KEY (driver_id)
        REFERENCES users(id)
);