package com.tajikGRP.Hansafar.rides.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RideJoinCreateRequest(

        @NotNull
        @Positive
        Short passengerCount,

        @NotNull
        @PositiveOrZero
        Short luggageCount,

        @PositiveOrZero
        Short largeLuggageCount,

        String specialLuggageNote
) {
}