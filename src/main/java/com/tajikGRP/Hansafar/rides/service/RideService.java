package com.tajikGRP.Hansafar.rides.service;

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
import com.tajikGRP.Hansafar.rides.repository.RideJoinRepository;
import com.tajikGRP.Hansafar.rides.repository.RideScheduleRepository;
import com.tajikGRP.Hansafar.users.domain.UserRole;
import com.tajikGRP.Hansafar.users.domain.Users;
import com.tajikGRP.Hansafar.users.dto.UserSummaryResponse;
import com.tajikGRP.Hansafar.users.repository.UsersRepository;
import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;
import com.tajikGRP.Hansafar.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideScheduleRepository rideScheduleRepository;
    private final RideJoinRepository rideJoinRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final UsersRepository usersRepository;

    /*
     * 기사 일정 생성
     */
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

        RideSchedule savedSchedule =
                rideScheduleRepository.save(schedule);

        return toScheduleResponse(savedSchedule);
    }

    /*
     * 현재 시간 이후의 전체 일정 조회
     */
    public List<RideScheduleResponse> getFutureSchedules() {
        return rideScheduleRepository
                .findAllByMeetingAtAfterOrderByMeetingAtAsc(
                        LocalDateTime.now()
                )
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    /*
     * 기사가 자신이 만든 일정 조회
     */
    public List<RideScheduleResponse> getDriverSchedules(
            Long driverId
    ) {
        if (!driverProfileRepository.existsById(driverId)) {
            throw new IllegalArgumentException(
                    "Driver not found."
            );
        }

        return rideScheduleRepository
                .findAllByDriverIdOrderByMeetingAtAsc(driverId)
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    /*
     * 승객 예약 요청 생성
     */
    @Transactional
    public RideJoinResponse createJoin(
            Long scheduleId,
            Long userId,
            RideJoinCreateRequest request
    ) {
        RideSchedule schedule =
                rideScheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ride schedule not found."
                                )
                        );

        Users user = usersRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );

        if (user.getRole() != UserRole.PASSENGER) {
            throw new IllegalStateException(
                    "Only passenger accounts can make reservations."
            );
        }

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

        long approvedPassengerCount =
                getApprovedPassengerCount(scheduleId);

        long remainingPassengerCount =
                schedule.getMaxPassengerCount()
                        - approvedPassengerCount;

        if (request.passengerCount()
                > remainingPassengerCount) {
            throw new IllegalStateException(
                    "The requested number of passengers exceeds the remaining seats."
            );
        }

        long totalFare = calculateTotalFare(
                schedule,
                request.passengerCount()
        );

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
                .specialLuggageNote(
                        request.specialLuggageNote()
                )
                .unitFareSnapshot(
                        schedule.getFareAmount()
                )
                .totalFare(totalFare)
                .status(RideJoinStatus.PENDING)
                .build();

        RideJoin savedJoin =
                rideJoinRepository.save(rideJoin);

        return toJoinResponse(savedJoin);
    }

    /*
     * 승객 자신의 예약 요청 목록
     */
    public List<RideJoinResponse> getUserJoins(
            Long userId
    ) {
        if (!usersRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "User not found."
            );
        }

        return rideJoinRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toJoinResponse)
                .toList();
    }

    /*
     * 특정 일정에 들어온 예약 요청 목록
     */
    public List<RideJoinResponse> getScheduleJoins(
            Long scheduleId,
            Long driverId
    ) {
        RideSchedule schedule =
                getScheduleOwnedByDriver(
                        scheduleId,
                        driverId
                );

        return rideJoinRepository
                .findAllByScheduleIdOrderByCreatedAtAsc(
                        schedule.getId()
                )
                .stream()
                .map(this::toJoinResponse)
                .toList();
    }

    /*
     * 기사 예약 승인
     */
    @Transactional
    public RideJoinResponse approveJoin(
            Long joinId,
            Long driverId
    ) {
        RideJoin rideJoin =
                rideJoinRepository.findById(joinId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation request not found."
                                )
                        );

        RideSchedule schedule =
                rideJoin.getSchedule();

        validateScheduleOwner(
                schedule,
                driverId
        );

        if (schedule.hasDeparted()) {
            throw new IllegalStateException(
                    "Reservations cannot be approved after the departure time."
            );
        }

        long approvedPassengerCount =
                getApprovedPassengerCount(
                        schedule.getId()
                );

        long remainingPassengerCount =
                schedule.getMaxPassengerCount()
                        - approvedPassengerCount;

        if (rideJoin.getPassengerCount()
                > remainingPassengerCount) {
            throw new IllegalStateException(
                    "This reservation cannot be approved because the requested number of passengers exceeds the remaining seats."
            );
        }

        long approvedLuggageCount =
                getApprovedLuggageCount(
                        schedule.getId()
                );

        long remainingLuggageCount =
                schedule.getMaxLuggageCount()
                        - approvedLuggageCount;

        if (rideJoin.getLuggageCount()
                > remainingLuggageCount) {
            throw new IllegalStateException(
                    "The remaining luggage capacity of the vehicle has been exceeded."
            );
        }

        rideJoin.approve();

        return toJoinResponse(rideJoin);
    }

    /*
     * 기사 예약 거절
     */
    @Transactional
    public RideJoinResponse rejectJoin(
            Long joinId,
            Long driverId
    ) {
        RideJoin rideJoin =
                rideJoinRepository.findById(joinId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation request not found."
                                )
                        );

        validateScheduleOwner(
                rideJoin.getSchedule(),
                driverId
        );

        rideJoin.reject();

        return toJoinResponse(rideJoin);
    }

    /*
     * 특정 일정이 해당 기사의 일정인지 확인
     */
    private RideSchedule getScheduleOwnedByDriver(
            Long scheduleId,
            Long driverId
    ) {
        RideSchedule schedule =
                rideScheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ride schedule not found."
                                )
                        );

        validateScheduleOwner(
                schedule,
                driverId
        );

        return schedule;
    }

    /*
     * 일정 소유 기사 확인
     */
    private void validateScheduleOwner(
            RideSchedule schedule,
            Long driverId
    ) {
        if (!schedule.getDriver()
                .getId()
                .equals(driverId)) {
            throw new IllegalStateException(
                    "Only the driver assigned to this schedule can perform this action."
            );
        }
    }

    /*
     * 승인된 전체 승객 수 계산
     */
    private long getApprovedPassengerCount(
            Long scheduleId
    ) {
        Long result =
                rideJoinRepository
                        .sumPassengerCountByScheduleIdAndStatus(
                                scheduleId,
                                RideJoinStatus.APPROVED
                        );

        return result == null ? 0L : result;
    }

    /*
     * 승인된 전체 수하물 수 계산
     */
    private long getApprovedLuggageCount(
            Long scheduleId
    ) {
        Long result =
                rideJoinRepository
                        .sumLuggageCountByScheduleIdAndStatus(
                                scheduleId,
                                RideJoinStatus.APPROVED
                        );

        return result == null ? 0L : result;
    }

    /*
     * 요금 계산
     */
    private long calculateTotalFare(
            RideSchedule schedule,
            short passengerCount
    ) {
        if (schedule.getFareType()
                == FareType.FIXED_TOTAL) {
            return schedule.getFareAmount();
        }

        return schedule.getFareAmount()
                * passengerCount;
    }

    /*
     * RideSchedule 엔티티를 응답 DTO로 변환
     */
    private RideScheduleResponse toScheduleResponse(
            RideSchedule schedule
    ) {
        long approvedPassengerCount =
                getApprovedPassengerCount(
                        schedule.getId()
                );

        long approvedLuggageCount =
                getApprovedLuggageCount(
                        schedule.getId()
                );

        long remainingPassengerCount =
                Math.max(
                        0,
                        schedule.getMaxPassengerCount()
                                - approvedPassengerCount
                );

        long remainingLuggageCount =
                Math.max(
                        0,
                        schedule.getMaxLuggageCount()
                                - approvedLuggageCount
                );

        return new RideScheduleResponse(
                schedule.getId(),

                DriverSummaryResponse.from(
                        schedule.getDriver()
                ),

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
                approvedPassengerCount,
                remainingPassengerCount,

                schedule.getMaxLuggageCount(),
                approvedLuggageCount,
                remainingLuggageCount,

                schedule.getDescription(),
                schedule.getStatus()
        );
    }

    /*
     * RideJoin 엔티티를 응답 DTO로 변환
     */
    private RideJoinResponse toJoinResponse(
            RideJoin rideJoin
    ) {
        RideSchedule schedule =
                rideJoin.getSchedule();

        return new RideJoinResponse(
                rideJoin.getId(),
                schedule.getId(),

                UserSummaryResponse.from(
                        rideJoin.getUser()
                ),

                rideJoin.getPassengerCount(),
                rideJoin.getLuggageCount(),
                rideJoin.getLargeLuggageCount(),
                rideJoin.getSpecialLuggageNote(),

                rideJoin.getUnitFareSnapshot(),
                rideJoin.getTotalFare(),

                rideJoin.getStatus(),
                rideJoin.getJoinedAt(),

                schedule.getMeetingAddress(),
                schedule.getMeetingPlaceName(),
                schedule.getDestinationAddress(),
                schedule.getDestinationPlaceName(),
                schedule.getMeetingAt()
        );
    }
}