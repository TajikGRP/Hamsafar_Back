package com.tajikGRP.Hansafar.vehicles.repository;

import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByDriverId(Long driverId);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(
            String licensePlate,
            Long vehicleId
    );
}