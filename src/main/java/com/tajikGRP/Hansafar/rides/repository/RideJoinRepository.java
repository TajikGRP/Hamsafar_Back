package com.tajikGRP.Hansafar.rides.repository;

import com.tajikGRP.Hansafar.rides.domain.RideJoin;
import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
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
import java.util.List;
import java.util.Optional;

public interface RideJoinRepository extends JpaRepository<RideJoin, Long> {

    @Query("SELECT r.schedule.id FROM RideJoin r WHERE r.id = :joinId")
    Optional<Long> findScheduleIdByJoinId(
            @Param("joinId") Long joinId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "schedule"})
    @Query("SELECT r FROM RideJoin r WHERE r.id = :joinId")
    Optional<RideJoin> findByIdForUpdate(
            @Param("joinId") Long joinId
    );

    @EntityGraph(attributePaths = {"user", "schedule"})
    Page<RideJoin> findAllByScheduleId(
            Long scheduleId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "schedule"})
    Page<RideJoin> findAllByUserId(
            Long userId,
            Pageable pageable
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
            SELECT COALESCE(SUM(r.passengerCount), 0)
            FROM RideJoin r
            WHERE r.schedule.id = :scheduleId
              AND r.status IN :statuses
            """)
    Long sumPassengerCountByScheduleIdAndStatuses(
            @Param("scheduleId") Long scheduleId,
            @Param("statuses") Collection<RideJoinStatus> statuses
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

    @Query("""
            SELECT COALESCE(SUM(r.luggageCount), 0)
            FROM RideJoin r
            WHERE r.schedule.id = :scheduleId
              AND r.status IN :statuses
            """)
    Long sumLuggageCountByScheduleIdAndStatuses(
            @Param("scheduleId") Long scheduleId,
            @Param("statuses") Collection<RideJoinStatus> statuses
    );

    boolean existsByScheduleIdAndUserIdAndStatusIn(
            Long scheduleId,
            Long userId,
            Collection<RideJoinStatus> statuses
    );

    List<RideJoin> findAllByScheduleIdAndStatusOrderByCreatedAtAscIdAsc(
            Long scheduleId,
            RideJoinStatus status
    );

    @Query("""
            SELECT COUNT(r) + 1
            FROM RideJoin r
            WHERE r.schedule.id = :scheduleId
              AND r.status = :status
              AND (
                    r.createdAt < :createdAt
                    OR (r.createdAt = :createdAt AND r.id < :joinId)
              )
            """)
    Long findWaitlistPosition(
            @Param("scheduleId") Long scheduleId,
            @Param("status") RideJoinStatus status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("joinId") Long joinId
    );

    @Query("""
            SELECT
                r.schedule.id AS scheduleId,
                r.status AS status,
                COALESCE(SUM(r.passengerCount), 0) AS passengerCount,
                COALESCE(SUM(r.luggageCount), 0) AS luggageCount,
                COUNT(r) AS requestCount
            FROM RideJoin r
            WHERE r.schedule.id IN :scheduleIds
              AND r.status IN :statuses
            GROUP BY r.schedule.id, r.status
            """)
    List<RideCapacitySummary> summarizeCapacityByScheduleIds(
            @Param("scheduleIds") Collection<Long> scheduleIds,
            @Param("statuses") Collection<RideJoinStatus> statuses
    );
}
