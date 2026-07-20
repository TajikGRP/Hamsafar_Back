package com.tajikGRP.Hansafar.drivers.repository;

import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository
        extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserId(Long userId);

    Optional<DriverProfile> findByIdAndUserDeletedAtIsNull(Long id);

    List<DriverProfile> findAllByUserDeletedAtIsNullOrderByIdAsc();
}