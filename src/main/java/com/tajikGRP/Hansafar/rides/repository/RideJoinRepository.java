package com.tajikGRP.Hansafar.rides.repository;

import com.tajikGRP.Hansafar.rides.domain.RideJoin;
import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RideJoinRepository extends JpaRepository<RideJoin, Long> {

    List<RideJoin> findAllByScheduleIdOrderByCreatedAtAsc(
            Long scheduleId
    );

    List<RideJoin> findAllByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(r.passengerCount), 0)
            FROM RideJoin r
            WHERE r.schedule.id = :scheduleId
              AND r.status = :status
            """)
    Long sumPassengerCountByScheduleIdAndStatus(
            @Param("scheduleId") Long scheduleId,
            @Param("status") RideJoinStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(r.luggageCount), 0)
            FROM RideJoin r
            WHERE r.schedule.id = :scheduleId
              AND r.status = :status
            """)
    Long sumLuggageCountByScheduleIdAndStatus(
            @Param("scheduleId") Long scheduleId,
            @Param("status") RideJoinStatus status
    );
}