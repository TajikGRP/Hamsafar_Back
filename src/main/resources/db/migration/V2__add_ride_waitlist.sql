ALTER TABLE app.ride_joins
    DROP CONSTRAINT ck_ride_joins_status;

ALTER TABLE app.ride_joins
    ADD CONSTRAINT ck_ride_joins_status
        CHECK (status IN (
            'PENDING',
            'WAITLISTED',
            'APPROVED',
            'REJECTED',
            'CANCELLED',
            'COMPLETED',
            'NO_SHOW'
        ));

CREATE UNIQUE INDEX uk_ride_joins_active_user_schedule
    ON app.ride_joins (schedule_id, user_id)
    WHERE status IN ('PENDING', 'WAITLISTED', 'APPROVED');

CREATE INDEX idx_ride_joins_waitlist_fifo
    ON app.ride_joins (schedule_id, joined_at, id)
    WHERE status = 'WAITLISTED';
