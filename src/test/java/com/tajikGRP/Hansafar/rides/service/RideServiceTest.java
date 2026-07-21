package com.tajikGRP.Hansafar.rides.service;

import com.tajikGRP.Hansafar.configuration.PageResponse;
import com.tajikGRP.Hansafar.drivers.dto.DriverCreateRequest;
import com.tajikGRP.Hansafar.drivers.dto.DriverResponse;
import com.tajikGRP.Hansafar.drivers.service.DriverService;
import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
import com.tajikGRP.Hansafar.rides.dto.RideJoinCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideJoinResponse;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleResponse;
import com.tajikGRP.Hansafar.rides.repository.RideJoinRepository;
import com.tajikGRP.Hansafar.users.dto.UserCreateRequest;
import com.tajikGRP.Hansafar.users.dto.UserResponse;
import com.tajikGRP.Hansafar.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RideServiceTest {

    @Autowired
    private RideService rideService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private UserService userService;

    @Autowired
    private RideJoinRepository rideJoinRepository;

    @Test
    void searchesAvailableSchedulesWithPagingAndSortingAndReturnsDetail() {
        DriverResponse driver = createDriver((short) 4, (short) 4);
        LocalDateTime meetingAt = LocalDateTime.now()
                .plusDays(2)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        RideScheduleResponse expensive = createSchedule(
                driver.driverId(),
                meetingAt,
                "Dushanbe Rudaki Avenue",
                "Khujand Center",
                300L
        );
        createSchedule(
                driver.driverId(),
                meetingAt.plusHours(1),
                "Dushanbe Airport",
                "Khujand Center",
                100L
        );
        createSchedule(
                driver.driverId(),
                meetingAt.plusHours(2),
                "Dushanbe Station",
                "Khujand Somoni Park",
                200L
        );
        createSchedule(
                driver.driverId(),
                meetingAt.plusDays(1),
                "Dushanbe Station",
                "Khujand Center",
                50L
        );

        PageResponse<RideScheduleResponse> result =
                rideService.searchSchedules(
                        "dushanbe",
                        "khujand",
                        meetingAt.toLocalDate(),
                        (short) 2,
                        0,
                        2,
                        "fareAmount",
                        "asc"
                );

        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.content())
                .extracting(RideScheduleResponse::fareAmount)
                .containsExactly(100L, 200L);

        RideScheduleResponse detail = rideService.getSchedule(
                expensive.scheduleId()
        );

        assertThat(detail.meetingAddress())
                .isEqualTo("Dushanbe Rudaki Avenue");
        assertThat(detail.destinationAddress())
                .isEqualTo("Khujand Center");
        assertThat(detail.remainingPassengerCount()).isEqualTo(4);
        assertThat(detail.waitlistedRequestCount()).isZero();
    }

    @Test
    void excludesSchedulesWithoutEnoughHeldCapacityFromSearch() {
        DriverResponse driver = createDriver((short) 2, (short) 2);
        LocalDateTime meetingAt = LocalDateTime.now().plusDays(2);
        RideScheduleResponse schedule = createSchedule(
                driver.driverId(),
                meetingAt,
                "Dushanbe",
                "Khujand",
                100L
        );
        UserResponse passenger = createPassenger("full@example.com");

        RideJoinResponse pending = rideService.createJoin(
                schedule.scheduleId(),
                passenger.userId(),
                joinRequest((short) 2, (short) 1)
        );

        assertThat(pending.status()).isEqualTo(RideJoinStatus.PENDING);

        PageResponse<RideScheduleResponse> result =
                rideService.searchSchedules(
                        "Dushanbe",
                        "Khujand",
                        meetingAt.toLocalDate(),
                        (short) 1,
                        0,
                        20,
                        "meetingAt",
                        "asc"
                );

        assertThat(result.content()).isEmpty();
    }

    @Test
    void waitlistsOverflowAndPromotesInFifoOrderAfterCancellation() {
        DriverResponse driver = createDriver((short) 2, (short) 2);
        RideScheduleResponse schedule = createSchedule(
                driver.driverId(),
                LocalDateTime.now().plusDays(2),
                "Dushanbe",
                "Khujand",
                100L
        );
        UserResponse occupyingPassenger =
                createPassenger("occupying@example.com");
        UserResponse firstWaitlisted =
                createPassenger("first-waitlisted@example.com");
        UserResponse secondWaitlisted =
                createPassenger("second-waitlisted@example.com");

        RideJoinResponse occupyingJoin = rideService.createJoin(
                schedule.scheduleId(),
                occupyingPassenger.userId(),
                joinRequest((short) 2, (short) 2)
        );
        RideJoinResponse firstJoin = rideService.createJoin(
                schedule.scheduleId(),
                firstWaitlisted.userId(),
                joinRequest((short) 1, (short) 1)
        );
        RideJoinResponse secondJoin = rideService.createJoin(
                schedule.scheduleId(),
                secondWaitlisted.userId(),
                joinRequest((short) 1, (short) 1)
        );

        assertThat(firstJoin.status()).isEqualTo(RideJoinStatus.WAITLISTED);
        assertThat(firstJoin.waitlistPosition()).isEqualTo(1L);
        assertThat(secondJoin.waitlistPosition()).isEqualTo(2L);

        RideJoinResponse cancelled = rideService.cancelJoin(
                occupyingJoin.joinId(),
                occupyingPassenger.userId()
        );

        assertThat(cancelled.status()).isEqualTo(RideJoinStatus.CANCELLED);
        assertThat(rideJoinRepository.findById(firstJoin.joinId()).orElseThrow()
                .getStatus()).isEqualTo(RideJoinStatus.PENDING);
        assertThat(rideJoinRepository.findById(secondJoin.joinId()).orElseThrow()
                .getStatus()).isEqualTo(RideJoinStatus.PENDING);

        RideScheduleResponse detail = rideService.getSchedule(
                schedule.scheduleId()
        );
        assertThat(detail.pendingPassengerCount()).isEqualTo(2);
        assertThat(detail.remainingPassengerCount()).isZero();
        assertThat(detail.waitlistedRequestCount()).isZero();
    }

    @Test
    void rejectsDuplicateActiveReservationForSameSchedule() {
        DriverResponse driver = createDriver((short) 2, (short) 2);
        RideScheduleResponse schedule = createSchedule(
                driver.driverId(),
                LocalDateTime.now().plusDays(2),
                "Dushanbe",
                "Khujand",
                100L
        );
        UserResponse passenger = createPassenger("duplicate@example.com");

        rideService.createJoin(
                schedule.scheduleId(),
                passenger.userId(),
                joinRequest((short) 1, (short) 0)
        );

        assertThatThrownBy(() -> rideService.createJoin(
                schedule.scheduleId(),
                passenger.userId(),
                joinRequest((short) 1, (short) 0)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("active reservation");
    }

    @Test
    void rejectsARequestThatCanNeverFitTheSchedule() {
        DriverResponse driver = createDriver((short) 2, (short) 2);
        RideScheduleResponse schedule = createSchedule(
                driver.driverId(),
                LocalDateTime.now().plusDays(2),
                "Dushanbe",
                "Khujand",
                100L
        );
        UserResponse passenger = createPassenger("oversized@example.com");

        assertThatThrownBy(() -> rideService.createJoin(
                schedule.scheduleId(),
                passenger.userId(),
                joinRequest((short) 3, (short) 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total schedule capacity");
    }

    private DriverResponse createDriver(
            short passengerCapacity,
            short luggageCapacity
    ) {
        return driverService.createDriver(
                new DriverCreateRequest(
                        "driver@example.com",
                        "Driver",
                        "+992900000001",
                        "+992900000001",
                        "Experienced driver",
                        "SEDAN",
                        "Toyota",
                        "Camry",
                        "TEST-001",
                        passengerCapacity,
                        400,
                        luggageCapacity,
                        luggageCapacity,
                        null
                )
        );
    }

    private UserResponse createPassenger(String email) {
        return userService.createUser(
                new UserCreateRequest(email, email)
        );
    }

    private RideScheduleResponse createSchedule(
            Long driverId,
            LocalDateTime meetingAt,
            String departure,
            String destination,
            long fare
    ) {
        return rideService.createSchedule(
                driverId,
                new RideScheduleCreateRequest(
                        "Dushanbe to Khujand",
                        meetingAt,
                        meetingAt.minusHours(1),
                        departure,
                        "Meeting point",
                        null,
                        null,
                        destination,
                        "Destination",
                        null,
                        null,
                        fare,
                        "Test schedule"
                )
        );
    }

    private RideJoinCreateRequest joinRequest(
            short passengerCount,
            short luggageCount
    ) {
        return new RideJoinCreateRequest(
                passengerCount,
                luggageCount,
                (short) 0,
                null
        );
    }
}
