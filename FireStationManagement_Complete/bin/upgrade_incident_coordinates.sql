-- Phase 2: add nullable Google Maps coordinates to the existing incident table.
-- Safe to run more than once. Existing incident, firefighter, dispatch and vehicle
-- rows are preserved; no table is dropped or recreated.
USE db_firestation_professional;

SET @schema_name = DATABASE();

SET @has_latitude = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'tbl_incident'
      AND COLUMN_NAME = 'latitude'
);
SET @sql_latitude = IF(
    @has_latitude = 0,
    'ALTER TABLE tbl_incident ADD COLUMN latitude DOUBLE NULL',
    'SELECT ''latitude already exists'' AS migration_status'
);
PREPARE add_latitude_stmt FROM @sql_latitude;
EXECUTE add_latitude_stmt;
DEALLOCATE PREPARE add_latitude_stmt;

SET @has_longitude = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'tbl_incident'
      AND COLUMN_NAME = 'longitude'
);
SET @sql_longitude = IF(
    @has_longitude = 0,
    'ALTER TABLE tbl_incident ADD COLUMN longitude DOUBLE NULL',
    'SELECT ''longitude already exists'' AS migration_status'
);
PREPARE add_longitude_stmt FROM @sql_longitude;
EXECUTE add_longitude_stmt;
DEALLOCATE PREPARE add_longitude_stmt;
