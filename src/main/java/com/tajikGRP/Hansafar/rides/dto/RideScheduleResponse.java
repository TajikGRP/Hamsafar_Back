package com.tajikGRP.Hansafar.rides.dto;

import com.tajikGRP.Hansafar.drivers.dto.DriverSummaryResponse;
import com.tajikGRP.Hansafar.rides.domain.FareType;
import com.tajikGRP.Hansafar.rides.domain.RideScheduleStatus;

import java.time.LocalDateTime;

public record RideScheduleResponse(
        Long scheduleId,

        DriverSummaryResponse driver,

        String title,
        LocalDateTime meetingAt,
        LocalDateTime joinDeadlineAt,

        String meetingAddress,
        String meetingPlaceName,
        String destinationAddress,
        String destinationPlaceName,

        FareType fareType,
        Long fareAmount,

        Short maxPassengerCount,
        Long approvedPassengerCount,
        Long pendingPassengerCount,
        Long remainingPassengerCount,

        Short maxLuggageCount,
        Long approvedLuggageCount,
        Long pendingLuggageCount,
        Long remainingLuggageCount,

        Long waitlistedRequestCount,

        String description,
        RideScheduleStatus status
) {
}
