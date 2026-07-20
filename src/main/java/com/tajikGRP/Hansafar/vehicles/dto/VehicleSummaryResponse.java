package com.tajikGRP.Hansafar.vehicles.dto;

import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;

public record VehicleSummaryResponse(
        Long vehicleId,
        String vehicleType,
        String manufacturer,
        String modelName,
        String licensePlate,
        Short passengerCapacity,
        Integer trunkVolumeLiter,
        String vehicleImageUrl,
        Boolean active
) {

    public static VehicleSummaryResponse from(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return new VehicleSummaryResponse(
                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getManufacturer(),
                vehicle.getModelName(),
                vehicle.getLicensePlate(),
                vehicle.getPassengerCapacity(),
                vehicle.getTrunkVolumeLiter(),
                vehicle.getVehicleImageUrl(),
                vehicle.getActive()
        );
    }
}