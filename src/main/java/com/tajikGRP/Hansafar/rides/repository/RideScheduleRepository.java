package com.tajikGRP.Hansafar.rides.repository;

import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
import com.tajikGRP.Hansafar.rides.domain.RideSchedule;
import com.tajikGRP.Hansafar.rides.domain.RideScheduleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface RideScheduleRepository
        extends JpaRepository<RideSchedule, Long> {

    @EntityGraph(attributePaths = {
            "driver",
            "driver.user",
            "driver.vehicle"
    })
    @Query("""
            SELECT schedule
            FROM RideSchedule schedule
            WHERE schedule.id = :scheduleId
            """)
    Optional<RideSchedule> findDetailedById(
            @Param("scheduleId") Long scheduleId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT schedule
            FROM RideSchedule schedule
            WHERE schedule.id = :scheduleId
            """)
    Optional<RideSchedule> findByIdForUpdate(
            @Param("scheduleId") Long scheduleId
    );

    @EntityGraph(attributePaths = {
            "driver",
            "driver.user",
            "driver.vehicle"
    })
    @Query(
            value = """
                    SELECT schedule
                    FROM RideSchedule schedule
                    WHERE schedule.meetingAt > :now
                      AND schedule.status = :status
                      AND schedule.driver.user.deletedAt IS NULL
                      AND schedule.driver.vehicle.active = true
                      AND (
                            :departure IS NULL
                            OR LOWER(schedule.meetingAddress)
                                LIKE LOWER(CONCAT('%', :departure, '%'))
                            OR LOWER(COALESCE(schedule.meetingPlaceName, ''))
                                LIKE LOWER(CONCAT('%', :departure, '%'))
                      )
                      AND (
                            :destination IS NULL
                            OR LOWER(schedule.destinationAddress)
                                LIKE LOWER(CONCAT('%', :destination, '%'))
                            OR LOWER(COALESCE(schedule.destinationPlaceName, ''))
                                LIKE LOWER(CONCAT('%', :destination, '%'))
                      )
                      AND (:dayStart IS NULL OR schedule.meetingAt >= :dayStart)
                      AND (:dayEnd IS NULL OR schedule.meetingAt < :dayEnd)
                      AND (
                            :passengerCount IS NULL
                            OR schedule.maxPassengerCount - COALESCE((
                                SELECT SUM(joinRequest.passengerCount)
                                FROM RideJoin joinRequest
                                WHERE joinRequest.schedule = schedule
                                  AND joinRequest.status IN :holdingStatuses
                            ), 0) >= :passengerCount
                      )
                    """,
            countQuery = """
                    SELECT COUNT(schedule)
                    FROM RideSchedule schedule
                    WHERE schedule.meetingAt > :now
                      AND schedule.status = :status
                      AND schedule.driver.user.deletedAt IS NULL
                      AND schedule.driver.vehicle.active = true
                      AND (
                            :departure IS NULL
                            OR LOWER(schedule.meetingAddress)
                                LIKE LOWER(CONCAT('%', :departure, '%'))
                            OR LOWER(COALESCE(schedule.meetingPlaceName, ''))
                                LIKE LOWER(CONCAT('%', :departure, '%'))
                      )
                      AND (
                            :destination IS NULL
                            OR LOWER(schedule.destinationAddress)
                                LIKE LOWER(CONCAT('%', :destination, '%'))
                            OR LOWER(COALESCE(schedule.destinationPlaceName, ''))
                                LIKE LOWER(CONCAT('%', :destination, '%'))
                      )
                      AND (:dayStart IS NULL OR schedule.meetingAt >= :dayStart)
                      AND (:dayEnd IS NULL OR schedule.meetingAt < :dayEnd)
                      AND (
                            :passengerCount IS NULL
                            OR schedule.maxPassengerCount - COALESCE((
                                SELECT SUM(joinRequest.passengerCount)
                                FROM RideJoin joinRequest
                                WHERE joinRequest.schedule = schedule
                                  AND joinRequest.status IN :holdingStatuses
                            ), 0) >= :passengerCount
                      )
                    """
    )
    Page<RideSchedule> searchAvailableSchedules(
            @Param("now") LocalDateTime now,
            @Param("status") RideScheduleStatus status,
            @Param("departure") String departure,
            @Param("destination") String destination,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("passengerCount") Short passengerCount,
            @Param("holdingStatuses") Collection<RideJoinStatus> holdingStatuses,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "driver",
            "driver.user",
            "driver.vehicle"
    })
    Page<RideSchedule> findAllByDriverId(
            Long driverId,
            Pageable pageable
    );
}
