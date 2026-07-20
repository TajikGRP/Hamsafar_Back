package com.tajikGRP.Hansafar.drivers.dto;

import java.time.LocalDateTime;

public record DriverResponse(
        Long driverId,
        Long userId,

        String email,
        String name,
        String phone,

        String driverPhone,
        String introduction,

        Long vehicleId,
        String vehicleType,
        String manufacturer,
        String modelName,
        String licensePlate,

        Short passengerCapacity,
        Integer trunkVolumeLiter,
        Short maxLuggageCount,
        Short largeLuggageCapacity,

        String vehicleImageUrl,
        Boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}