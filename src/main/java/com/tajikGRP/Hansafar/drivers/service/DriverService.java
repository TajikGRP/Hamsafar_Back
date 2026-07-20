package com.tajikGRP.Hansafar.drivers.service;

import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import com.tajikGRP.Hansafar.drivers.dto.DriverCreateRequest;
import com.tajikGRP.Hansafar.drivers.dto.DriverResponse;
import com.tajikGRP.Hansafar.drivers.dto.DriverUpdateRequest;
import com.tajikGRP.Hansafar.drivers.repository.DriverProfileRepository;
import com.tajikGRP.Hansafar.users.domain.UserRole;
import com.tajikGRP.Hansafar.users.domain.Users;
import com.tajikGRP.Hansafar.users.repository.UsersRepository;
import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;
import com.tajikGRP.Hansafar.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public DriverResponse createDriver(
            DriverCreateRequest request
    ) {
        if (usersRepository.existsByEmail(request.email())) {
            throw new IllegalStateException(
                    "Email is already in use."
            );
        }

        if (vehicleRepository.existsByLicensePlate(
                request.licensePlate()
        )) {
            throw new IllegalStateException(
                    "License plate is already in use."
            );
        }

        Users user = Users.builder()
                .email(request.email())
                .name(request.name())
                .phone(request.phone())
                .role(UserRole.DRIVER)
                .build();

        Users savedUser = usersRepository.save(user);

        DriverProfile driver = DriverProfile.builder()
                .user(savedUser)
                .driverPhone(request.driverPhone())
                .introduction(request.introduction())
                .build();

        DriverProfile savedDriver =
                driverProfileRepository.save(driver);

        Vehicle vehicle = Vehicle.builder()
                .driver(savedDriver)
                .vehicleType(request.vehicleType())
                .manufacturer(request.manufacturer())
                .modelName(request.modelName())
                .licensePlate(request.licensePlate())
                .passengerCapacity(request.passengerCapacity())
                .trunkVolumeLiter(request.trunkVolumeLiter())
                .maxLuggageCount(request.maxLuggageCount())
                .largeLuggageCapacity(
                        request.largeLuggageCapacity()
                )
                .vehicleImageUrl(request.vehicleImageUrl())
                .active(true)
                .build();

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        return toResponse(savedDriver, savedVehicle);
    }

    public List<DriverResponse> getDrivers() {
        return driverProfileRepository
                .findAllByUserDeletedAtIsNullOrderByIdAsc()
                .stream()
                .map(driver -> {
                    Vehicle vehicle = vehicleRepository
                            .findByDriverId(driver.getId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Vehicle not found."
                                    )
                            );

                    return toResponse(driver, vehicle);
                })
                .toList();
    }

    public DriverResponse getDriver(Long driverId) {
        DriverProfile driver = findActiveDriver(driverId);
        Vehicle vehicle = findVehicle(driverId);

        return toResponse(driver, vehicle);
    }

    @Transactional
    public DriverResponse updateDriver(
            Long driverId,
            DriverUpdateRequest request
    ) {
        DriverProfile driver = findActiveDriver(driverId);
        Vehicle vehicle = findVehicle(driverId);
        Users user = driver.getUser();

        if (usersRepository.existsByEmailAndIdNot(
                request.email(),
                user.getId()
        )) {
            throw new IllegalStateException(
                    "Email is already in use."
            );
        }

        if (vehicleRepository.existsByLicensePlateAndIdNot(
                request.licensePlate(),
                vehicle.getId()
        )) {
            throw new IllegalStateException(
                    "License plate is already in use."
            );
        }

        user.updateDriverInfo(
                request.email(),
                request.name(),
                request.phone()
        );

        driver.update(
                request.driverPhone(),
                request.introduction()
        );

        vehicle.update(
                request.vehicleType(),
                request.manufacturer(),
                request.modelName(),
                request.licensePlate(),
                request.passengerCapacity(),
                request.trunkVolumeLiter(),
                request.maxLuggageCount(),
                request.largeLuggageCapacity(),
                request.vehicleImageUrl()
        );

        return toResponse(driver, vehicle);
    }

    @Transactional
    public void deleteDriver(Long driverId) {
        DriverProfile driver = findActiveDriver(driverId);
        Vehicle vehicle = findVehicle(driverId);

        driver.getUser().softDelete();
        vehicle.deactivate();
    }

    private DriverProfile findActiveDriver(Long driverId) {
        return driverProfileRepository
                .findByIdAndUserDeletedAtIsNull(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Driver not found."
                        )
                );
    }

    private Vehicle findVehicle(Long driverId) {
        return vehicleRepository
                .findByDriverId(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found."
                        )
                );
    }

    private DriverResponse toResponse(
            DriverProfile driver,
            Vehicle vehicle
    ) {
        Users user = driver.getUser();

        return new DriverResponse(
                driver.getId(),
                user.getId(),

                user.getEmail(),
                user.getName(),
                user.getPhone(),

                driver.getDriverPhone(),
                driver.getIntroduction(),

                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getManufacturer(),
                vehicle.getModelName(),
                vehicle.getLicensePlate(),

                vehicle.getPassengerCapacity(),
                vehicle.getTrunkVolumeLiter(),
                vehicle.getMaxLuggageCount(),
                vehicle.getLargeLuggageCapacity(),

                vehicle.getVehicleImageUrl(),
                vehicle.getActive(),

                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}