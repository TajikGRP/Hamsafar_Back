package com.tajikGRP.Hansafar.drivers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DriverUpdateRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 30)
        String phone,

        @NotBlank
        @Size(max = 30)
        String driverPhone,

        String introduction,

        @NotBlank
        @Size(max = 50)
        String vehicleType,

        @Size(max = 50)
        String manufacturer,

        @NotBlank
        @Size(max = 100)
        String modelName,

        @NotBlank
        @Size(max = 30)
        String licensePlate,

        @NotNull
        @Positive
        Short passengerCapacity,

        @PositiveOrZero
        Integer trunkVolumeLiter,

        @NotNull
        @PositiveOrZero
        Short maxLuggageCount,

        @PositiveOrZero
        Short largeLuggageCapacity,

        @Size(max = 500)
        String vehicleImageUrl
) {
}