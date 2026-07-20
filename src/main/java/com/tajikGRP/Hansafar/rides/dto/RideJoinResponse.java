package com.tajikGRP.Hansafar.rides.dto;

import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;
import com.tajikGRP.Hansafar.users.dto.UserSummaryResponse;

import java.time.LocalDateTime;

public record RideJoinResponse(
        Long joinId,
        Long scheduleId,

        UserSummaryResponse user,

        Short passengerCount,
        Short luggageCount,
        Short largeLuggageCount,
        String specialLuggageNote,

        Long unitFare,
        Long totalFare,

        RideJoinStatus status,
        LocalDateTime joinedAt,

        String meetingAddress,
        String meetingPlaceName,
        String destinationAddress,
        String destinationPlaceName,
        LocalDateTime meetingAt
) {
}