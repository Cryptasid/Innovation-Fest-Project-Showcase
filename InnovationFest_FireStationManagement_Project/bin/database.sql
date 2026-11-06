-- Fresh-install schema for Fire Station Management System.
-- This file intentionally contains NO DROP statements. For an existing installation,
-- do not rerun this file; apply the relevant upgrade scripts instead.
CREATE DATABASE IF NOT EXISTS db_firestation_professional;
USE db_firestation_professional;

CREATE TABLE IF NOT EXISTS tbl_firefighter (
    firefighter_id CHAR(8) PRIMARY KEY,
    ff_name VARCHAR(100) NOT NULL,
    ff_contact VARCHAR(15) NOT NULL,
    ff_expertise ENUM('Rescuer','Medic','Electric Technician','Chemical Technician') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tbl_incident (
    incident_id INT PRIMARY KEY AUTO_INCREMENT,
    in_threat VARCHAR(100) NOT NULL,
    in_street VARCHAR(100) NOT NULL,
    in_city VARCHAR(100) NOT NULL,
    incident_date DATE NOT NULL,
    in_severity ENUM('Critical','High','Medium','Low') NOT NULL DEFAULT 'Medium',
    in_status ENUM('Active','Investigating','Resolved') NOT NULL DEFAULT 'Active',
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tbl_vehicle (
    vehicle_id VARCHAR(20) PRIMARY KEY,
    vehicle_type ENUM('Pumper Fire Truck','Ladder Truck','Water Tanker','Brush Engine','Rescue / Squad Truck') NOT NULL,
    registration_number VARCHAR(40) NOT NULL UNIQUE,
    vehicle_status ENUM('Available','Dispatched','Maintenance') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tbl_dispatch (
    dispatch_id INT PRIMARY KEY AUTO_INCREMENT,
    firefighter_id CHAR(8) NOT NULL,
    incident_id INT NOT NULL,
    vehicle_id VARCHAR(20) NULL,
    dispatch_date DATE NOT NULL,
    dispatch_status ENUM('Active','Completed') NOT NULL DEFAULT 'Active',
    dispatched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dispatch_firefighter FOREIGN KEY (firefighter_id)
        REFERENCES tbl_firefighter(firefighter_id) ON DELETE CASCADE,
    CONSTRAINT fk_dispatch_incident FOREIGN KEY (incident_id)
        REFERENCES tbl_incident(incident_id) ON DELETE CASCADE,
    CONSTRAINT fk_dispatch_vehicle FOREIGN KEY (vehicle_id)
        REFERENCES tbl_vehicle(vehicle_id) ON DELETE SET NULL
) ENGINE=InnoDB;
