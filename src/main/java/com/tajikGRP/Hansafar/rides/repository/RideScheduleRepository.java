package com.tajikGRP.Hansafar.rides.repository;

import com.tajikGRP.Hansafar.rides.domain.RideSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RideScheduleRepository
        extends JpaRepository<RideSchedule, Long> {

    List<RideSchedule> findAllByMeetingAtAfterOrderByMeetingAtAsc(
            LocalDateTime currentTime
    );

    List<RideSchedule> findAllByDriverIdOrderByMeetingAtAsc(
            Long driverId
    );
}