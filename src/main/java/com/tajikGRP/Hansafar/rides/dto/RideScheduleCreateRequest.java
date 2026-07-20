package com.tajikGRP.Hansafar.rides.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideScheduleCreateRequest(

        String title,

        @NotNull
        @Future
        LocalDateTime meetingAt,

        LocalDateTime joinDeadlineAt,

        @NotBlank
        String meetingAddress,

        String meetingPlaceName,

        BigDecimal meetingLatitude,

        BigDecimal meetingLongitude,

        @NotBlank
        String destinationAddress,

        String destinationPlaceName,

        BigDecimal destinationLatitude,

        BigDecimal destinationLongitude,

        @NotNull
        @PositiveOrZero
        Long fareAmount,

        String description
) {
}