package com.tajikGRP.Hansafar.rides.service;

import com.tajikGRP.Hansafar.configuration.PageRequestFactory;
import com.tajikGRP.Hansafar.configuration.PageResponse;
import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import com.tajikGRP.Hansafar.drivers.dto.DriverSummaryResponse;
import com.tajikGRP.Hansafar.drivers.repository.DriverProfileRepository;
import com.tajikGRP.Hansafar.rides.domain.FareType;
import com.tajikGRP.Hansafar.rides.domain.RideJoin;
import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
import com.tajikGRP.Hansafar.rides.domain.RideSchedule;
import com.tajikGRP.Hansafar.rides.domain.RideScheduleStatus;
import com.tajikGRP.Hansafar.rides.dto.RideJoinCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideJoinResponse;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleResponse;
import com.tajikGRP.Hansafar.rides.repository.RideCapacitySummary;
import com.tajikGRP.Hansafar.rides.repository.RideJoinRepository;
import com.tajikGRP.Hansafar.rides.repository.RideScheduleRepository;
import com.tajikGRP.Hansafar.users.domain.UserRole;
import com.tajikGRP.Hansafar.users.domain.Users;
import com.tajikGRP.Hansafar.users.dto.UserSummaryResponse;
import com.tajikGRP.Hansafar.users.repository.UsersRepository;
import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;
import com.tajikGRP.Hansafar.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private static final Set<RideJoinStatus> CAPACITY_HOLDING_STATUSES =
            Set.of(RideJoinStatus.PENDING, RideJoinStatus.APPROVED);

    private static final Set<RideJoinStatus> ACTIVE_JOIN_STATUSES =
            Set.of(
                    RideJoinStatus.PENDING,
                    RideJoinStatus.WAITLISTED,
                    RideJoinStatus.APPROVED
            );

    private static final Set<RideJoinStatus> CAPACITY_SUMMARY_STATUSES =
            Set.of(
                    RideJoinStatus.PENDING,
                    RideJoinStatus.APPROVED,
                    RideJoinStatus.WAITLISTED
            );

    private static final Set<String> SCHEDULE_SORT_FIELDS =
            Set.of("meetingAt", "fareAmount", "createdAt");

    private static final Set<String> JOIN_SORT_FIELDS =
            Set.of("createdAt", "status", "totalFare");

    private final RideScheduleRepository rideScheduleRepository;
    private final RideJoinRepository rideJoinRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public RideScheduleResponse createSchedule(
            Long driverId,
            RideScheduleCreateRequest request
    ) {
        DriverProfile driver = driverProfileRepository.findById(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Driver not found.")
                );

        if (driver.getUser().getRole() != UserRole.DRIVER) {
            throw new IllegalStateException(
                    "Only driver accounts can create schedules."
            );
        }

        Vehicle vehicle = vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No registered vehicle was found."
                        )
                );

        if (!Boolean.TRUE.equals(vehicle.getActive())) {
            throw new IllegalStateException(
                    "Only active vehicles can be used."
            );
        }

        if (!request.meetingAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "The departure time must be in the future."
            );
        }

        if (request.joinDeadlineAt() != null) {
            if (!request.joinDeadlineAt().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "The reservation deadline must be in the future."
                );
            }

            if (request.joinDeadlineAt().isAfter(request.meetingAt())) {
                throw new IllegalArgumentException(
                        "The reservation deadline cannot be later than the departure time."
                );
            }
        }

        RideSchedule schedule = RideSchedule.builder()
                .driver(driver)
                .title(request.title())
                .meetingAt(request.meetingAt())
                .joinDeadlineAt(request.joinDeadlineAt())
                .meetingAddress(request.meetingAddress())
                .meetingPlaceName(request.meetingPlaceName())
                .meetingLatitude(request.meetingLatitude())
                .meetingLongitude(request.meetingLongitude())
                .destinationAddress(request.destinationAddress())
                .destinationPlaceName(request.destinationPlaceName())
                .destinationLatitude(request.destinationLatitude())
                .destinationLongitude(request.destinationLongitude())
                .fareType(FareType.PER_PERSON)
                .fareAmount(request.fareAmount())
                .maxPassengerCount(vehicle.getPassengerCapacity())
                .maxLuggageCount(vehicle.getMaxLuggageCount())
                .description(request.description())
                .status(RideScheduleStatus.OPEN)
                .build();

        RideSchedule savedSchedule = rideScheduleRepository.save(schedule);
        return toScheduleResponse(savedSchedule, CapacitySnapshot.empty());
    }

    public RideScheduleResponse getSchedule(Long scheduleId) {
        RideSchedule schedule = rideScheduleRepository
                .findDetailedById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride schedule not found.")
                );

        return toScheduleResponse(
                schedule,
                getCapacitySnapshot(scheduleId)
        );
    }

    public PageResponse<RideScheduleResponse> searchSchedules(
            String departure,
            String destination,
            LocalDate date,
            Short passengerCount,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PageRequestFactory.create(
                page,
                size,
                sortBy,
                sortDirection,
                SCHEDULE_SORT_FIELDS
        );

        LocalDateTime dayStart = date == null
                ? null
                : date.atStartOfDay();
        LocalDateTime dayEnd = date == null
                ? null
                : date.plusDays(1).atStartOfDay();

        Page<RideSchedule> schedules = rideScheduleRepository
                .searchAvailableSchedules(
                        LocalDateTime.now(),
                        RideScheduleStatus.OPEN,
                        normalizeSearchTerm(departure),
                        normalizeSearchTerm(destination),
                        dayStart,
                        dayEnd,
                        passengerCount,
                        CAPACITY_HOLDING_STATUSES,
                        pageable
                );

        Map<Long, CapacitySnapshot> capacities = getCapacitySnapshots(
                schedules.getContent()
                        .stream()
                        .map(RideSchedule::getId)
                        .toList()
        );

        return PageResponse.from(
                schedules.map(schedule ->
                        toScheduleResponse(
                                schedule,
                                capacities.getOrDefault(
                                        schedule.getId(),
                                        CapacitySnapshot.empty()
                                )
                        )
                )
        );
    }

    public PageResponse<RideScheduleResponse> getDriverSchedules(
            Long driverId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (!driverProfileRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found.");
        }

        Pageable pageable = PageRequestFactory.create(
                page,
                size,
                sortBy,
                sortDirection,
                SCHEDULE_SORT_FIELDS
        );

        Page<RideSchedule> schedules = rideScheduleRepository
                .findAllByDriverId(driverId, pageable);
        Map<Long, CapacitySnapshot> capacities = getCapacitySnapshots(
                schedules.getContent()
                        .stream()
                        .map(RideSchedule::getId)
                        .toList()
        );

        return PageResponse.from(
                schedules.map(schedule ->
                        toScheduleResponse(
                                schedule,
                                capacities.getOrDefault(
                                        schedule.getId(),
                                        CapacitySnapshot.empty()
                                )
                        )
                )
        );
    }

    @Transactional
    public RideJoinResponse createJoin(
            Long scheduleId,
            Long userId,
            RideJoinCreateRequest request
    ) {
        RideSchedule schedule = rideScheduleRepository
                .findByIdForUpdate(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride schedule not found.")
                );

        Users user = usersRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );

        if (user.getRole() != UserRole.PASSENGER) {
            throw new IllegalStateException(
                    "Only passenger accounts can make reservations."
            );
        }

        validateScheduleOpenForJoin(schedule);
        validateRequestCanFitSchedule(schedule, request);

        if (rideJoinRepository.existsByScheduleIdAndUserIdAndStatusIn(
                scheduleId,
                userId,
                ACTIVE_JOIN_STATUSES
        )) {
            throw new IllegalStateException(
                    "An active reservation already exists for this schedule."
            );
        }

        long heldPassengerCount = getHeldPassengerCount(scheduleId);
        long heldLuggageCount = getHeldLuggageCount(scheduleId);
        boolean capacityAvailable =
                heldPassengerCount + request.passengerCount()
                        <= schedule.getMaxPassengerCount()
                && heldLuggageCount + request.luggageCount()
                        <= schedule.getMaxLuggageCount();

        RideJoinStatus initialStatus = capacityAvailable
                ? RideJoinStatus.PENDING
                : RideJoinStatus.WAITLISTED;

        RideJoin rideJoin = RideJoin.builder()
                .schedule(schedule)
                .user(user)
                .passengerCount(request.passengerCount())
                .luggageCount(request.luggageCount())
                .largeLuggageCount(
                        request.largeLuggageCount() == null
                                ? 0
                                : request.largeLuggageCount()
                )
                .specialLuggageNote(request.specialLuggageNote())
                .unitFareSnapshot(schedule.getFareAmount())
                .totalFare(calculateTotalFare(
                        schedule,
                        request.passengerCount()
                ))
                .status(initialStatus)
                .build();

        RideJoin savedJoin = rideJoinRepository.saveAndFlush(rideJoin);
        return toJoinResponse(savedJoin);
    }

    public PageResponse<RideJoinResponse> getUserJoins(
            Long userId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        if (!usersRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found.");
        }

        Pageable pageable = PageRequestFactory.create(
                page,
                size,
                sortBy,
                sortDirection,
                JOIN_SORT_FIELDS
        );

        return PageResponse.from(
                rideJoinRepository.findAllByUserId(userId, pageable)
                        .map(this::toJoinResponse)
        );
    }

    public PageResponse<RideJoinResponse> getScheduleJoins(
            Long scheduleId,
            Long driverId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        RideSchedule schedule = getScheduleOwnedByDriver(
                scheduleId,
                driverId
        );
        Pageable pageable = PageRequestFactory.create(
                page,
                size,
                sortBy,
                sortDirection,
                JOIN_SORT_FIELDS
        );

        return PageResponse.from(
                rideJoinRepository.findAllByScheduleId(
                                schedule.getId(),
                                pageable
                        )
                        .map(this::toJoinResponse)
        );
    }

    @Transactional
    public RideJoinResponse approveJoin(
            Long joinId,
            Long driverId
    ) {
        RideSchedule schedule = lockJoinSchedule(joinId);
        RideJoin rideJoin = getJoinForUpdate(joinId);

        validateScheduleOwner(schedule, driverId);

        if (schedule.hasDeparted()) {
            throw new IllegalStateException(
                    "Reservations cannot be approved after the departure time."
            );
        }

        if (getHeldPassengerCount(schedule.getId())
                > schedule.getMaxPassengerCount()) {
            throw new IllegalStateException(
                    "The schedule is already over passenger capacity."
            );
        }

        if (getHeldLuggageCount(schedule.getId())
                > schedule.getMaxLuggageCount()) {
            throw new IllegalStateException(
                    "The schedule is already over luggage capacity."
            );
        }

        rideJoin.approve();
        return toJoinResponse(rideJoin);
    }

    @Transactional
    public RideJoinResponse rejectJoin(
            Long joinId,
            Long driverId
    ) {
        RideSchedule schedule = lockJoinSchedule(joinId);
        RideJoin rideJoin = getJoinForUpdate(joinId);
        validateScheduleOwner(schedule, driverId);

        boolean releasedCapacity = rideJoin.holdsCapacity();
        rideJoin.reject();

        if (releasedCapacity) {
            promoteWaitlist(schedule);
        }

        return toJoinResponse(rideJoin);
    }

    @Transactional
    public RideJoinResponse cancelJoin(
            Long joinId,
            Long userId
    ) {
        RideSchedule schedule = lockJoinSchedule(joinId);
        RideJoin rideJoin = getJoinForUpdate(joinId);

        if (!rideJoin.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "Only the passenger who made the reservation can cancel it."
            );
        }

        if (schedule.hasDeparted()) {
            throw new IllegalStateException(
                    "Reservations cannot be cancelled after departure."
            );
        }

        boolean releasedCapacity = rideJoin.holdsCapacity();
        rideJoin.cancel();

        if (releasedCapacity) {
            promoteWaitlist(schedule);
        }

        return toJoinResponse(rideJoin);
    }

    private void validateScheduleOpenForJoin(RideSchedule schedule) {
        if (schedule.hasDeparted()) {
            throw new IllegalStateException(
                    "The departure time for this schedule has already passed."
            );
        }

        if (schedule.isJoinClosed()) {
            throw new IllegalStateException(
                    "Reservation requests for this schedule are closed."
            );
        }

        if (schedule.getStatus() != RideScheduleStatus.OPEN) {
            throw new IllegalStateException(
                    "This schedule is not currently available for reservation."
            );
        }
    }

    private void validateRequestCanFitSchedule(
            RideSchedule schedule,
            RideJoinCreateRequest request
    ) {
        if (request.passengerCount() > schedule.getMaxPassengerCount()) {
            throw new IllegalArgumentException(
                    "The requested passenger count exceeds the total schedule capacity."
            );
        }

        if (request.luggageCount() > schedule.getMaxLuggageCount()) {
            throw new IllegalArgumentException(
                    "The requested luggage count exceeds the total schedule capacity."
            );
        }

        short largeLuggageCount = request.largeLuggageCount() == null
                ? 0
                : request.largeLuggageCount();
        if (largeLuggageCount > request.luggageCount()) {
            throw new IllegalArgumentException(
                    "Large luggage count cannot exceed total luggage count."
            );
        }
    }

    private RideJoin getJoinForUpdate(Long joinId) {
        return rideJoinRepository.findByIdForUpdate(joinId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation request not found."
                        )
                );
    }

    private RideSchedule lockJoinSchedule(Long joinId) {
        Long scheduleId = rideJoinRepository.findScheduleIdByJoinId(joinId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation request not found."
                        )
                );
        return lockSchedule(scheduleId);
    }

    private RideSchedule lockSchedule(Long scheduleId) {
        return rideScheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride schedule not found.")
                );
    }

    private RideSchedule getScheduleOwnedByDriver(
            Long scheduleId,
            Long driverId
    ) {
        RideSchedule schedule = rideScheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride schedule not found.")
                );

        validateScheduleOwner(schedule, driverId);
        return schedule;
    }

    private void validateScheduleOwner(
            RideSchedule schedule,
            Long driverId
    ) {
        if (!schedule.getDriver().getId().equals(driverId)) {
            throw new IllegalStateException(
                    "Only the driver assigned to this schedule can perform this action."
            );
        }
    }

    private void promoteWaitlist(RideSchedule schedule) {
        long heldPassengerCount = getHeldPassengerCount(schedule.getId());
        long heldLuggageCount = getHeldLuggageCount(schedule.getId());
        List<RideJoin> waitlistedJoins = rideJoinRepository
                .findAllByScheduleIdAndStatusOrderByCreatedAtAscIdAsc(
                        schedule.getId(),
                        RideJoinStatus.WAITLISTED
                );

        for (RideJoin waitlistedJoin : waitlistedJoins) {
            boolean fitsPassengerCapacity =
                    heldPassengerCount + waitlistedJoin.getPassengerCount()
                            <= schedule.getMaxPassengerCount();
            boolean fitsLuggageCapacity =
                    heldLuggageCount + waitlistedJoin.getLuggageCount()
                            <= schedule.getMaxLuggageCount();

            if (!fitsPassengerCapacity || !fitsLuggageCapacity) {
                break;
            }

            waitlistedJoin.promoteFromWaitlist();
            heldPassengerCount += waitlistedJoin.getPassengerCount();
            heldLuggageCount += waitlistedJoin.getLuggageCount();
        }
    }

    private long getHeldPassengerCount(Long scheduleId) {
        Long result = rideJoinRepository
                .sumPassengerCountByScheduleIdAndStatuses(
                        scheduleId,
                        CAPACITY_HOLDING_STATUSES
                );
        return result == null ? 0L : result;
    }

    private long getHeldLuggageCount(Long scheduleId) {
        Long result = rideJoinRepository
                .sumLuggageCountByScheduleIdAndStatuses(
                        scheduleId,
                        CAPACITY_HOLDING_STATUSES
                );
        return result == null ? 0L : result;
    }

    private long calculateTotalFare(
            RideSchedule schedule,
            short passengerCount
    ) {
        if (schedule.getFareType() == FareType.FIXED_TOTAL) {
            return schedule.getFareAmount();
        }

        return schedule.getFareAmount() * passengerCount;
    }

    private String normalizeSearchTerm(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CapacitySnapshot getCapacitySnapshot(Long scheduleId) {
        return getCapacitySnapshots(List.of(scheduleId))
                .getOrDefault(scheduleId, CapacitySnapshot.empty());
    }

    private Map<Long, CapacitySnapshot> getCapacitySnapshots(
            Collection<Long> scheduleIds
    ) {
        Map<Long, CapacitySnapshot> snapshots = new HashMap<>();
        if (scheduleIds.isEmpty()) {
            return snapshots;
        }

        for (RideCapacitySummary summary : rideJoinRepository
                .summarizeCapacityByScheduleIds(
                        scheduleIds,
                        CAPACITY_SUMMARY_STATUSES
                )) {
            CapacitySnapshot current = snapshots.getOrDefault(
                    summary.getScheduleId(),
                    CapacitySnapshot.empty()
            );
            snapshots.put(
                    summary.getScheduleId(),
                    current.withSummary(summary)
            );
        }
        return snapshots;
    }

    private RideScheduleResponse toScheduleResponse(
            RideSchedule schedule,
            CapacitySnapshot capacity
    ) {
        long remainingPassengerCount = Math.max(
                0,
                schedule.getMaxPassengerCount()
                        - capacity.approvedPassengerCount()
                        - capacity.pendingPassengerCount()
        );
        long remainingLuggageCount = Math.max(
                0,
                schedule.getMaxLuggageCount()
                        - capacity.approvedLuggageCount()
                        - capacity.pendingLuggageCount()
        );

        return new RideScheduleResponse(
                schedule.getId(),
                DriverSummaryResponse.from(schedule.getDriver()),
                schedule.getTitle(),
                schedule.getMeetingAt(),
                schedule.getJoinDeadlineAt(),
                schedule.getMeetingAddress(),
                schedule.getMeetingPlaceName(),
                schedule.getDestinationAddress(),
                schedule.getDestinationPlaceName(),
                schedule.getFareType(),
                schedule.getFareAmount(),
                schedule.getMaxPassengerCount(),
                capacity.approvedPassengerCount(),
                capacity.pendingPassengerCount(),
                remainingPassengerCount,
                schedule.getMaxLuggageCount(),
                capacity.approvedLuggageCount(),
                capacity.pendingLuggageCount(),
                remainingLuggageCount,
                capacity.waitlistedRequestCount(),
                schedule.getDescription(),
                schedule.getStatus()
        );
    }

    private RideJoinResponse toJoinResponse(RideJoin rideJoin) {
        RideSchedule schedule = rideJoin.getSchedule();
        Long waitlistPosition = null;

        if (rideJoin.getStatus() == RideJoinStatus.WAITLISTED) {
            waitlistPosition = rideJoinRepository.findWaitlistPosition(
                    schedule.getId(),
                    RideJoinStatus.WAITLISTED,
                    rideJoin.getCreatedAt(),
                    rideJoin.getId()
            );
        }

        return new RideJoinResponse(
                rideJoin.getId(),
                schedule.getId(),
                UserSummaryResponse.from(rideJoin.getUser()),
                rideJoin.getPassengerCount(),
                rideJoin.getLuggageCount(),
                rideJoin.getLargeLuggageCount(),
                rideJoin.getSpecialLuggageNote(),
                rideJoin.getUnitFareSnapshot(),
                rideJoin.getTotalFare(),
                rideJoin.getStatus(),
                waitlistPosition,
                rideJoin.getJoinedAt(),
                schedule.getMeetingAddress(),
                schedule.getMeetingPlaceName(),
                schedule.getDestinationAddress(),
                schedule.getDestinationPlaceName(),
                schedule.getMeetingAt()
        );
    }

    private record CapacitySnapshot(
            long approvedPassengerCount,
            long pendingPassengerCount,
            long approvedLuggageCount,
            long pendingLuggageCount,
            long waitlistedRequestCount
    ) {

        private static CapacitySnapshot empty() {
            return new CapacitySnapshot(0, 0, 0, 0, 0);
        }

        private CapacitySnapshot withSummary(RideCapacitySummary summary) {
            return switch (summary.getStatus()) {
                case APPROVED -> new CapacitySnapshot(
                        summary.getPassengerCount(),
                        pendingPassengerCount,
                        summary.getLuggageCount(),
                        pendingLuggageCount,
                        waitlistedRequestCount
                );
                case PENDING -> new CapacitySnapshot(
                        approvedPassengerCount,
                        summary.getPassengerCount(),
                        approvedLuggageCount,
                        summary.getLuggageCount(),
                        waitlistedRequestCount
                );
                case WAITLISTED -> new CapacitySnapshot(
                        approvedPassengerCount,
                        pendingPassengerCount,
                        approvedLuggageCount,
                        pendingLuggageCount,
                        summary.getRequestCount()
                );
                default -> this;
            };
        }
    }
}
