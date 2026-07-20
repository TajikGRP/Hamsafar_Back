package com.tajikGRP.Hansafar.drivers.dto;

import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import com.tajikGRP.Hansafar.vehicles.dto.VehicleSummaryResponse;

public record DriverSummaryResponse(
        Long driverId,
        String name,
        String email,
        String driverPhone,
        VehicleSummaryResponse vehicle
) {

    public static DriverSummaryResponse from(
            DriverProfile driver
    ) {
        return new DriverSummaryResponse(
                driver.getId(),
                driver.getUser().getName(),
                driver.getUser().getEmail(),
                driver.getDriverPhone(),
                VehicleSummaryResponse.from(driver.getVehicle())
        );
    }
}