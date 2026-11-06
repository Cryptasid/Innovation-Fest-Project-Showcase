-- Run this once on an existing db_firestation_professional database.
-- This script preserves all existing firefighter, incident, and dispatch records.

USE db_firestation_professional;

CREATE TABLE tbl_vehicle (
    vehicle_id VARCHAR(20) PRIMARY KEY,
    vehicle_type ENUM(
        'Pumper Fire Truck',
        'Ladder Truck',
        'Water Tanker',
        'Brush Engine',
        'Rescue / Squad Truck'
    ) NOT NULL,
    registration_number VARCHAR(40) NOT NULL UNIQUE,
    vehicle_status ENUM('Available', 'Dispatched', 'Maintenance') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB;

ALTER TABLE tbl_dispatch
    ADD COLUMN vehicle_id VARCHAR(20) NULL AFTER incident_id,
    ADD INDEX idx_dispatch_vehicle_status (vehicle_id, dispatch_status),
    ADD CONSTRAINT fk_dispatch_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES tbl_vehicle(vehicle_id)
        ON DELETE SET NULL;
