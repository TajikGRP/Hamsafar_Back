package com.tajikGRP.Hansafar.rides.controller;

import com.tajikGRP.Hansafar.rides.dto.RideJoinCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideJoinResponse;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleCreateRequest;
import com.tajikGRP.Hansafar.rides.dto.RideScheduleResponse;
import com.tajikGRP.Hansafar.rides.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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

    /*
     * 현재 시간 이후 전체 일정 조회
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<RideScheduleResponse>> getFutureSchedules() {
        return ResponseEntity.ok(
                rideService.getFutureSchedules()
        );
    }

    /*
     * 기사 자신의 일정 목록 조회
     */
    @GetMapping("/drivers/{driverId}/schedules")
    public ResponseEntity<List<RideScheduleResponse>> getDriverSchedules(
            @PathVariable Long driverId
    ) {
        return ResponseEntity.ok(
                rideService.getDriverSchedules(driverId)
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
    public ResponseEntity<List<RideJoinResponse>> getUserJoins(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                rideService.getUserJoins(userId)
        );
    }

    /*
     * 특정 일정의 예약 요청 목록 조회
     */
    @GetMapping("/schedules/{scheduleId}/joins")
    public ResponseEntity<List<RideJoinResponse>> getScheduleJoins(
            @PathVariable Long scheduleId,
            @RequestParam Long driverId
    ) {
        return ResponseEntity.ok(
                rideService.getScheduleJoins(scheduleId, driverId)
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
}