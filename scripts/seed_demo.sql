DO $$
DECLARE
    demo_now TIMESTAMP WITHOUT TIME ZONE := now() AT TIME ZONE 'Asia/Dushanbe';
    demo_date DATE := (now() AT TIME ZONE 'Asia/Dushanbe')::date;
    driver_farid_user_id BIGINT;
    driver_zafar_user_id BIGINT;
    passenger_amina_id BIGINT;
    passenger_rustam_id BIGINT;
    passenger_madina_id BIGINT;
    passenger_kamol_id BIGINT;
    driver_farid_id BIGINT;
    driver_zafar_id BIGINT;
    schedule_khujand_id BIGINT;
    schedule_hisor_id BIGINT;
    schedule_dushanbe_id BIGINT;
BEGIN
    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.driver.farid@hamsafar.test', 'Farid Rahmonov', 'DRIVER', '+992900001001', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO driver_farid_user_id;

    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.driver.zafar@hamsafar.test', 'Zafar Karimov', 'DRIVER', '+992900001002', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO driver_zafar_user_id;

    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.passenger.amina@hamsafar.test', 'Amina Saidova', 'PASSENGER', '+992900002001', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO passenger_amina_id;

    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.passenger.rustam@hamsafar.test', 'Rustam Nazarov', 'PASSENGER', '+992900002002', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO passenger_rustam_id;

    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.passenger.madina@hamsafar.test', 'Madina Yusufova', 'PASSENGER', '+992900002003', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO passenger_madina_id;

    INSERT INTO app.users (email, name, role, phone, deleted_at, created_at, updated_at)
    VALUES ('demo.passenger.kamol@hamsafar.test', 'Kamol Sharipov', 'PASSENGER', '+992900002004', NULL, demo_now, demo_now)
    ON CONFLICT (email) DO UPDATE
        SET name = EXCLUDED.name,
            role = EXCLUDED.role,
            phone = EXCLUDED.phone,
            deleted_at = NULL,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO passenger_kamol_id;

    INSERT INTO app.driver_profiles (user_id, driver_phone, introduction, created_at, updated_at)
    VALUES (driver_farid_user_id, '+992900001001', 'Dushanbe-Khujand route demo driver', demo_now, demo_now)
    ON CONFLICT (user_id) DO UPDATE
        SET driver_phone = EXCLUDED.driver_phone,
            introduction = EXCLUDED.introduction,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO driver_farid_id;

    INSERT INTO app.driver_profiles (user_id, driver_phone, introduction, created_at, updated_at)
    VALUES (driver_zafar_user_id, '+992900001002', 'Hisor and Dushanbe route demo driver', demo_now, demo_now)
    ON CONFLICT (user_id) DO UPDATE
        SET driver_phone = EXCLUDED.driver_phone,
            introduction = EXCLUDED.introduction,
            updated_at = EXCLUDED.updated_at
    RETURNING id INTO driver_zafar_id;

    INSERT INTO app.vehicles (
        driver_id, vehicle_type, manufacturer, model_name, license_plate,
        passenger_capacity, trunk_volume_liter, max_luggage_count,
        large_luggage_capacity, vehicle_image_url, is_active, created_at, updated_at
    )
    VALUES (
        driver_farid_id, 'SEDAN', 'Toyota', 'Camry', 'DEMO-001',
        4, 493, 4, 2, NULL, TRUE, demo_now, demo_now
    )
    ON CONFLICT (driver_id) DO UPDATE
        SET vehicle_type = EXCLUDED.vehicle_type,
            manufacturer = EXCLUDED.manufacturer,
            model_name = EXCLUDED.model_name,
            license_plate = EXCLUDED.license_plate,
            passenger_capacity = EXCLUDED.passenger_capacity,
            trunk_volume_liter = EXCLUDED.trunk_volume_liter,
            max_luggage_count = EXCLUDED.max_luggage_count,
            large_luggage_capacity = EXCLUDED.large_luggage_capacity,
            is_active = TRUE,
            updated_at = EXCLUDED.updated_at;

    INSERT INTO app.vehicles (
        driver_id, vehicle_type, manufacturer, model_name, license_plate,
        passenger_capacity, trunk_volume_liter, max_luggage_count,
        large_luggage_capacity, vehicle_image_url, is_active, created_at, updated_at
    )
    VALUES (
        driver_zafar_id, 'VAN', 'Hyundai', 'Staria', 'DEMO-002',
        7, 831, 8, 4, NULL, TRUE, demo_now, demo_now
    )
    ON CONFLICT (driver_id) DO UPDATE
        SET vehicle_type = EXCLUDED.vehicle_type,
            manufacturer = EXCLUDED.manufacturer,
            model_name = EXCLUDED.model_name,
            license_plate = EXCLUDED.license_plate,
            passenger_capacity = EXCLUDED.passenger_capacity,
            trunk_volume_liter = EXCLUDED.trunk_volume_liter,
            max_luggage_count = EXCLUDED.max_luggage_count,
            large_luggage_capacity = EXCLUDED.large_luggage_capacity,
            is_active = TRUE,
            updated_at = EXCLUDED.updated_at;

    SELECT id INTO schedule_khujand_id
    FROM app.ride_schedules
    WHERE title = '[DEMO] Dushanbe to Khujand - Morning'
    ORDER BY id
    LIMIT 1;

    IF schedule_khujand_id IS NULL THEN
        INSERT INTO app.ride_schedules (
            driver_id, title, meeting_at, join_deadline_at,
            meeting_address, meeting_place_name, meeting_latitude, meeting_longitude,
            destination_address, destination_place_name, destination_latitude, destination_longitude,
            fare_type, fare_amount, max_passenger_count, max_luggage_count,
            description, status, created_at, updated_at
        )
        VALUES (
            driver_farid_id, '[DEMO] Dushanbe to Khujand - Morning',
            demo_date + 2 + TIME '09:00', demo_date + 2 + TIME '08:00',
            'Rudaki Avenue 80, Dushanbe', 'Dushanbe Opera House', 38.5598000, 68.7870000,
            'Ismoil Somoni Avenue, Khujand', 'Panjshanbe Bazaar', 40.2833000, 69.6222000,
            'PER_PERSON', 120, 4, 4,
            'Demo ride with approved, pending, and waitlisted reservations.', 'OPEN', demo_now, demo_now
        )
        RETURNING id INTO schedule_khujand_id;
    ELSE
        UPDATE app.ride_schedules
        SET driver_id = driver_farid_id,
            meeting_at = demo_date + 2 + TIME '09:00',
            join_deadline_at = demo_date + 2 + TIME '08:00',
            meeting_address = 'Rudaki Avenue 80, Dushanbe',
            meeting_place_name = 'Dushanbe Opera House',
            meeting_latitude = 38.5598000,
            meeting_longitude = 68.7870000,
            destination_address = 'Ismoil Somoni Avenue, Khujand',
            destination_place_name = 'Panjshanbe Bazaar',
            destination_latitude = 40.2833000,
            destination_longitude = 69.6222000,
            fare_type = 'PER_PERSON',
            fare_amount = 120,
            max_passenger_count = 4,
            max_luggage_count = 4,
            description = 'Demo ride with approved, pending, and waitlisted reservations.',
            status = 'OPEN',
            updated_at = demo_now
        WHERE id = schedule_khujand_id;
    END IF;

    SELECT id INTO schedule_hisor_id
    FROM app.ride_schedules
    WHERE title = '[DEMO] Dushanbe to Hisor - Evening'
    ORDER BY id
    LIMIT 1;

    IF schedule_hisor_id IS NULL THEN
        INSERT INTO app.ride_schedules (
            driver_id, title, meeting_at, join_deadline_at,
            meeting_address, meeting_place_name, meeting_latitude, meeting_longitude,
            destination_address, destination_place_name, destination_latitude, destination_longitude,
            fare_type, fare_amount, max_passenger_count, max_luggage_count,
            description, status, created_at, updated_at
        )
        VALUES (
            driver_zafar_id, '[DEMO] Dushanbe to Hisor - Evening',
            demo_date + 3 + TIME '18:00', demo_date + 3 + TIME '17:00',
            'Rudaki Avenue 127, Dushanbe', 'Sadreddin Ayni Park', 38.5965000, 68.7864000,
            'Hisor Central Road, Hisor', 'Hisor Fortress', 38.5250000, 68.5517000,
            'PER_PERSON', 40, 7, 8,
            'Demo evening ride with spare capacity.', 'OPEN', demo_now, demo_now
        )
        RETURNING id INTO schedule_hisor_id;
    ELSE
        UPDATE app.ride_schedules
        SET driver_id = driver_zafar_id,
            meeting_at = demo_date + 3 + TIME '18:00',
            join_deadline_at = demo_date + 3 + TIME '17:00',
            meeting_address = 'Rudaki Avenue 127, Dushanbe',
            meeting_place_name = 'Sadreddin Ayni Park',
            meeting_latitude = 38.5965000,
            meeting_longitude = 68.7864000,
            destination_address = 'Hisor Central Road, Hisor',
            destination_place_name = 'Hisor Fortress',
            destination_latitude = 38.5250000,
            destination_longitude = 68.5517000,
            fare_type = 'PER_PERSON',
            fare_amount = 40,
            max_passenger_count = 7,
            max_luggage_count = 8,
            description = 'Demo evening ride with spare capacity.',
            status = 'OPEN',
            updated_at = demo_now
        WHERE id = schedule_hisor_id;
    END IF;

    SELECT id INTO schedule_dushanbe_id
    FROM app.ride_schedules
    WHERE title = '[DEMO] Khujand to Dushanbe - Morning'
    ORDER BY id
    LIMIT 1;

    IF schedule_dushanbe_id IS NULL THEN
        INSERT INTO app.ride_schedules (
            driver_id, title, meeting_at, join_deadline_at,
            meeting_address, meeting_place_name, meeting_latitude, meeting_longitude,
            destination_address, destination_place_name, destination_latitude, destination_longitude,
            fare_type, fare_amount, max_passenger_count, max_luggage_count,
            description, status, created_at, updated_at
        )
        VALUES (
            driver_farid_id, '[DEMO] Khujand to Dushanbe - Morning',
            demo_date + 4 + TIME '08:30', demo_date + 4 + TIME '07:30',
            'Ismoil Somoni Avenue, Khujand', 'Panjshanbe Bazaar', 40.2833000, 69.6222000,
            'Rudaki Avenue 80, Dushanbe', 'Dushanbe Opera House', 38.5598000, 68.7870000,
            'PER_PERSON', 130, 4, 4,
            'Demo return ride without reservations.', 'OPEN', demo_now, demo_now
        )
        RETURNING id INTO schedule_dushanbe_id;
    ELSE
        UPDATE app.ride_schedules
        SET driver_id = driver_farid_id,
            meeting_at = demo_date + 4 + TIME '08:30',
            join_deadline_at = demo_date + 4 + TIME '07:30',
            meeting_address = 'Ismoil Somoni Avenue, Khujand',
            meeting_place_name = 'Panjshanbe Bazaar',
            meeting_latitude = 40.2833000,
            meeting_longitude = 69.6222000,
            destination_address = 'Rudaki Avenue 80, Dushanbe',
            destination_place_name = 'Dushanbe Opera House',
            destination_latitude = 38.5598000,
            destination_longitude = 68.7870000,
            fare_type = 'PER_PERSON',
            fare_amount = 130,
            max_passenger_count = 4,
            max_luggage_count = 4,
            description = 'Demo return ride without reservations.',
            status = 'OPEN',
            updated_at = demo_now
        WHERE id = schedule_dushanbe_id;
    END IF;

    INSERT INTO app.ride_joins (
        schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
        special_luggage_note, unit_fare_snapshot, total_fare, status,
        cancelled_at, joined_at, updated_at
    )
    VALUES (
        schedule_khujand_id, passenger_amina_id, 2, 1, 0,
        NULL, 120, 240, 'APPROVED', NULL, demo_now - INTERVAL '30 minutes', demo_now
    )
    ON CONFLICT (schedule_id, user_id)
        WHERE status IN ('PENDING', 'WAITLISTED', 'APPROVED')
    DO UPDATE SET
        passenger_count = EXCLUDED.passenger_count,
        luggage_count = EXCLUDED.luggage_count,
        large_luggage_count = EXCLUDED.large_luggage_count,
        special_luggage_note = EXCLUDED.special_luggage_note,
        unit_fare_snapshot = EXCLUDED.unit_fare_snapshot,
        total_fare = EXCLUDED.total_fare,
        status = EXCLUDED.status,
        cancelled_at = NULL,
        updated_at = EXCLUDED.updated_at;

    INSERT INTO app.ride_joins (
        schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
        special_luggage_note, unit_fare_snapshot, total_fare, status,
        cancelled_at, joined_at, updated_at
    )
    VALUES (
        schedule_khujand_id, passenger_rustam_id, 1, 1, 0,
        NULL, 120, 120, 'PENDING', NULL, demo_now - INTERVAL '20 minutes', demo_now
    )
    ON CONFLICT (schedule_id, user_id)
        WHERE status IN ('PENDING', 'WAITLISTED', 'APPROVED')
    DO UPDATE SET
        passenger_count = EXCLUDED.passenger_count,
        luggage_count = EXCLUDED.luggage_count,
        large_luggage_count = EXCLUDED.large_luggage_count,
        special_luggage_note = EXCLUDED.special_luggage_note,
        unit_fare_snapshot = EXCLUDED.unit_fare_snapshot,
        total_fare = EXCLUDED.total_fare,
        status = EXCLUDED.status,
        cancelled_at = NULL,
        updated_at = EXCLUDED.updated_at;

    INSERT INTO app.ride_joins (
        schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
        special_luggage_note, unit_fare_snapshot, total_fare, status,
        cancelled_at, joined_at, updated_at
    )
    VALUES (
        schedule_khujand_id, passenger_madina_id, 2, 1, 0,
        NULL, 120, 240, 'WAITLISTED', NULL, demo_now - INTERVAL '10 minutes', demo_now
    )
    ON CONFLICT (schedule_id, user_id)
        WHERE status IN ('PENDING', 'WAITLISTED', 'APPROVED')
    DO UPDATE SET
        passenger_count = EXCLUDED.passenger_count,
        luggage_count = EXCLUDED.luggage_count,
        large_luggage_count = EXCLUDED.large_luggage_count,
        special_luggage_note = EXCLUDED.special_luggage_note,
        unit_fare_snapshot = EXCLUDED.unit_fare_snapshot,
        total_fare = EXCLUDED.total_fare,
        status = EXCLUDED.status,
        cancelled_at = NULL,
        updated_at = EXCLUDED.updated_at;

    INSERT INTO app.ride_joins (
        schedule_id, user_id, passenger_count, luggage_count, large_luggage_count,
        special_luggage_note, unit_fare_snapshot, total_fare, status,
        cancelled_at, joined_at, updated_at
    )
    VALUES (
        schedule_hisor_id, passenger_kamol_id, 2, 2, 1,
        'One large suitcase', 40, 80, 'APPROVED', NULL, demo_now - INTERVAL '5 minutes', demo_now
    )
    ON CONFLICT (schedule_id, user_id)
        WHERE status IN ('PENDING', 'WAITLISTED', 'APPROVED')
    DO UPDATE SET
        passenger_count = EXCLUDED.passenger_count,
        luggage_count = EXCLUDED.luggage_count,
        large_luggage_count = EXCLUDED.large_luggage_count,
        special_luggage_note = EXCLUDED.special_luggage_note,
        unit_fare_snapshot = EXCLUDED.unit_fare_snapshot,
        total_fare = EXCLUDED.total_fare,
        status = EXCLUDED.status,
        cancelled_at = NULL,
        updated_at = EXCLUDED.updated_at;
END
$$;
