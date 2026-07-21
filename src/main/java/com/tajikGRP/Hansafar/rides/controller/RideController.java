package com.tajikGRP.Hansafar.rides.controller;

import com.tajikGRP.Hansafar.configuration.PageResponse;
import com.tajikGRP.Hansafar.rides.dto.RideJoinCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideJoinResponse;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleResponse;
import com.tajikGRP.Hansafar.rides.service.RideService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    /*
     * 기사 일정 생성
     */
    @PostMapping("/schedules")
    public ResponseEntity<RideScheduleResponse> createSchedule(
            @RequestParam Long driverId,
            @Valid @RequestBody RideScheduleCreateRequest request
    ) {
        RideScheduleResponse response =
                rideService.createSchedule(driverId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* 출발지·목적지·날짜·인원수 기반 일정 검색 */
    @GetMapping("/schedules")
    public ResponseEntity<PageResponse<RideScheduleResponse>> searchSchedules(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) @Positive Short passengerCount,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "meetingAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(
                rideService.searchSchedules(
                        departure,
                        destination,
                        date,
                        passengerCount,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /* 일정 상세 조회 */
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<RideScheduleResponse> getSchedule(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(
                rideService.getSchedule(scheduleId)
        );
    }

    /*
     * 기사 자신의 일정 목록 조회
     */
    @GetMapping("/drivers/{driverId}/schedules")
    public ResponseEntity<PageResponse<RideScheduleResponse>> getDriverSchedules(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "meetingAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(
                rideService.getDriverSchedules(
                        driverId,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /*
     * 승객 예약 요청 생성
     */
    @PostMapping("/schedules/{scheduleId}/joins")
    public ResponseEntity<RideJoinResponse> createJoin(
            @PathVariable Long scheduleId,
            @RequestParam Long userId,
            @Valid @RequestBody RideJoinCreateRequest request
    ) {
        RideJoinResponse response =
                rideService.createJoin(scheduleId, userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * 승객 자신의 예약 요청 목록 조회
     */
    @GetMapping("/users/{userId}/joins")
    public ResponseEntity<PageResponse<RideJoinResponse>> getUserJoins(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(
                rideService.getUserJoins(
                        userId,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /*
     * 특정 일정의 예약 요청 목록 조회
     */
    @GetMapping("/schedules/{scheduleId}/joins")
    public ResponseEntity<PageResponse<RideJoinResponse>> getScheduleJoins(
            @PathVariable Long scheduleId,
            @RequestParam Long driverId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(
                rideService.getScheduleJoins(
                        scheduleId,
                        driverId,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    /*
     * 기사 예약 승인
     */
    @PatchMapping("/joins/{joinId}/approve")
    public ResponseEntity<RideJoinResponse> approveJoin(
            @PathVariable Long joinId,
            @RequestParam Long driverId
    ) {
        return ResponseEntity.ok(
                rideService.approveJoin(joinId, driverId)
        );
    }

    /*
     * 기사 예약 거절
     */
    @PatchMapping("/joins/{joinId}/reject")
    public ResponseEntity<RideJoinResponse> rejectJoin(
            @PathVariable Long joinId,
            @RequestParam Long driverId
    ) {
        return ResponseEntity.ok(
                rideService.rejectJoin(joinId, driverId)
        );
    }

    /* 승객 예약 취소 및 대기열 자동 승급 */
    @PatchMapping("/joins/{joinId}/cancel")
    public ResponseEntity<RideJoinResponse> cancelJoin(
            @PathVariable Long joinId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                rideService.cancelJoin(joinId, userId)
        );
    }
}
