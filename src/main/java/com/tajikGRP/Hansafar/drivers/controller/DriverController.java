package com.tajikGRP.Hansafar.drivers.controller;

import com.tajikGRP.Hansafar.drivers.dto.DriverCreateRequest;
import com.tajikGRP.Hansafar.drivers.dto.DriverResponse;
import com.tajikGRP.Hansafar.drivers.dto.DriverUpdateRequest;
import com.tajikGRP.Hansafar.drivers.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody DriverCreateRequest request
    ) {
        DriverResponse response =
                driverService.createDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getDrivers() {
        return ResponseEntity.ok(
                driverService.getDrivers()
        );
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriver(
            @PathVariable Long driverId
    ) {
        return ResponseEntity.ok(
                driverService.getDriver(driverId)
        );
    }

    @PutMapping("/{driverId}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverUpdateRequest request
    ) {
        return ResponseEntity.ok(
                driverService.updateDriver(driverId, request)
        );
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable Long driverId
    ) {
        driverService.deleteDriver(driverId);

        return ResponseEntity.noContent().build();
    }
}