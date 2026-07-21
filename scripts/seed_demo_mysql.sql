USE hamsafar;

SET @demo_now = NOW();

INSERT INTO users (email, name, role, phone, deleted_at, created_at, updated_at)
VALUES ('demo.driver@hamsafar.local', 'Farid Rahmonov', 'DRIVER', '+992900001001', NULL, @demo_now, @demo_now)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    name = VALUES(name),
    role = VALUES(role),
    phone = VALUES(phone),
    deleted_at = NULL,
    updated_at = VALUES(updated_at);
SET @driver_user_id = LAST_INSERT_ID();

INSERT INTO users (email, name, role, phone, deleted_at, created_at, updated_at)
VALUES ('demo.amina@hamsafar.local', 'Amina Saidova', 'PASSENGER', '+992900002001', NULL, @demo_now, @demo_now)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    name = VALUES(name),
    role = VALUES(role),
    phone = VALUES(phone),
    deleted_at = NULL,
    updated_at = VALUES(updated_at);
SET @amina_id = LAST_INSERT_ID();

INSERT INTO users (email, name, role, phone, deleted_at, created_at, updated_at)
VALUES ('demo.rustam@hamsafar.local', 'Rustam Nazarov', 'PASSENGER', '+992900002002', NULL, @demo_now, @demo_now)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    name = VALUES(name),
    role = VALUES(role),
    phone = VALUES(phone),
    deleted_at = NULL,
    updated_at = VALUES(updated_at);
SET @rustam_id = LAST_INSERT_ID();

INSERT INTO users (email, name, role, phone, deleted_at, created_at, updated_at)
VALUES ('demo.madina@hamsafar.local', 'Madina Yusufova', 'PASSENGER', '+992900002003', NULL, @demo_now, @demo_now)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    name = VALUES(name),
    role = VALUES(role),
    phone = VALUES(phone),
    deleted_at = NULL,
    updated_at = VALUES(updated_at);
SET @madina_id = LAST_INSERT_ID();

INSERT INTO driver_profiles (user_id, driver_phone, introduction, created_at, updated_at)
VALUES (@driver_user_id, '+992900001001', 'Local demo driver', @demo_now, @demo_now)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    driver_phone = VALUES(driver_phone),
    introduction = VALUES(introduction),
    updated_at = VALUES(updated_at);
SET @driver_id = LAST_INSERT_ID();

INSERT INTO vehicles (
    driver_id, vehicle_type, manufacturer, model_name, license_plate,
    passenger_capacity, trunk_volume_liter, max_luggage_count,
    large_luggage_capacity, vehicle_image_url, is_active, created_at, updated_at
)
VALUES (
    @driver_id, 'SEDAN', 'Toyota', 'Camry', 'LOCAL-DEMO-001',
    4, 493, 4, 2, NULL, TRUE, @demo_now, @demo_now
)
ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    vehicle_type = VALUES(vehicle_type),
    manufacturer = VALUES(manufacturer),
    model_name = VALUES(model_name),
    passenger_capacity = VALUES(passenger_capacity),
    trunk_volume_liter = VALUES(trunk_volume_liter),
    max_luggage_count = VALUES(max_luggage_count),
    large_luggage_capacity = VALUES(large_luggage_capacity),
    is_active = TRUE,
    updated_at = VALUES(updated_at);

INSERT INTO ride_schedules (
    driver_id, title, meeting_at, join_deadline_at,
    meeting_address, meeting_place_name, meeting_latitude, meeting_longitude,
    destination_address, destination_place_name, destination_latitude, destination_longitude,
    fare_type, fare_amount, max_passenger_count, max_luggage_count,
    description, status, created_at, updated_at
)
SELECT
    @driver_id, '[LOCAL DEMO] Dushanbe to Khujand',
    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00'),
    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00'),
    'Rudaki Avenue 80, Dushanbe', 'Dushanbe Opera House', 38.5598000, 68.7870000,
    'Ismoil Somoni Avenue, Khujand', 'Panjshanbe Bazaar', 40.2833000, 69.6222000,
    'PER_PERSON', 120, 4, 4,
    'Approved, pending, and waitlisted reservations for local testing.', 'OPEN', @demo_now, @demo_now
WHERE NOT EXISTS (
    SELECT 1 FROM ride_schedules WHERE title = '[LOCAL DEMO] Dushanbe to Khujand'
);

SET @khujand_schedule_id = (
    SELECT id FROM ride_schedules
    WHERE title = '[LOCAL DEMO] Dushanbe to Khujand'
    ORDER BY id LIMIT 1
);

UPDATE ride_schedules
SET meeting_at = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00'),
    join_deadline_at = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00'),
    status = 'OPEN',
    updated_at = @demo_now
WHERE id = @khujand_schedule_id;

INSERT INTO ride_schedules (
    driver_id, title, meeting_at, join_deadline_at,
    meeting_address, meeting_place_name, meeting_latitude, meeting_longitude,
    destination_address, destination_place_name, destination_latitude, destination_longitude,
    fare_type, fare_amount, max_passenger_count, max_luggage_count,
    description, status, created_at, updated_at
)
SELECT
    @driver_id, '[LOCAL DEMO] Khujand to Dushanbe',
    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:30:00'),
    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '07:30:00'),
    'Ismoil Somoni Avenue, Khujand', 'Panjshanbe Bazaar', 40.2833000, 69.6222000,
    'Rudaki Avenue 80, Dushanbe', 'Dushanbe Opera House', 38.5598000, 68.7870000,
    'PER_PERSON', 130, 4, 4,
    'Return route for local search testing.', 'OPEN', @demo_now, @demo_now
WHERE NOT EXISTS (
    SELECT 1 FROM ride_schedules WHERE title = '[LOCAL DEMO] Khujand to Dushanbe'
);

INSERT INTO ride_joins (
    schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
    special_luggage_note, unit_fare_snapshot, total_fare, status,
    cancelled_at, joined_at, updated_at
)
SELECT @khujand_schedule_id, @amina_id, 2, 1, 0, NULL, 120, 240,
       'APPROVED', NULL, DATE_SUB(@demo_now, INTERVAL 30 MINUTE), @demo_now
WHERE NOT EXISTS (
    SELECT 1 FROM ride_joins
    WHERE schedule_id = @khujand_schedule_id AND user_id = @amina_id
      AND status IN ('PENDING', 'WAITLISTED', 'APPROVED')
);

INSERT INTO ride_joins (
    schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
    special_luggage_note, unit_fare_snapshot, total_fare, status,
    cancelled_at, joined_at, updated_at
)
SELECT @khujand_schedule_id, @rustam_id, 1, 1, 0, NULL, 120, 120,
       'PENDING', NULL, DATE_SUB(@demo_now, INTERVAL 20 MINUTE), @demo_now
WHERE NOT EXISTS (
    SELECT 1 FROM ride_joins
    WHERE schedule_id = @khujand_schedule_id AND user_id = @rustam_id
      AND status IN ('PENDING', 'WAITLISTED', 'APPROVED')
);

INSERT INTO ride_joins (
    schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
    special_luggage_note, unit_fare_snapshot, total_fare, status,
    cancelled_at, joined_at, updated_at
)
SELECT @khujand_schedule_id, @madina_id, 2, 1, 0, NULL, 120, 240,
       'WAITLISTED', NULL, DATE_SUB(@demo_now, INTERVAL 10 MINUTE), @demo_now
WHERE NOT EXISTS (
    SELECT 1 FROM ride_joins
    WHERE schedule_id = @khujand_schedule_id AND user_id = @madina_id
      AND status IN ('PENDING', 'WAITLISTED', 'APPROVED')
);

SELECT 'users' AS entity, COUNT(*) AS row_count
FROM users WHERE email LIKE 'demo.%@hamsafar.local'
UNION ALL
SELECT 'drivers', COUNT(*) FROM driver_profiles WHERE id = @driver_id
UNION ALL
SELECT 'schedules', COUNT(*) FROM ride_schedules WHERE title LIKE '[LOCAL DEMO]%'
UNION ALL
SELECT 'reservations', COUNT(*) FROM ride_joins WHERE schedule_id = @khujand_schedule_id;
