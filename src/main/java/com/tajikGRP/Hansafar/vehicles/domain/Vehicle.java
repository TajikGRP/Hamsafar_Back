package com.tajikGRP.Hansafar.vehicles.domain;

import com.tajikGRP.Hansafar.configuration.BaseEntity;
import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private DriverProfile driver;

    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Column(length = 50)
    private String manufacturer;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "license_plate", nullable = false, unique = true, length = 30)
    private String licensePlate;

    @Positive
    @Column(name = "passenger_capacity", nullable = false)
    private Short passengerCapacity;

    @PositiveOrZero
    @Column(name = "trunk_volume_liter")
    private Integer trunkVolumeLiter;

    @PositiveOrZero
    @Column(name = "max_luggage_count", nullable = false)
    private Short maxLuggageCount;

    @PositiveOrZero
    @Column(name = "large_luggage_capacity")
    private Short largeLuggageCapacity;

    @Column(name = "vehicle_image_url", length = 500)
    private String vehicleImageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    public void update(
            String vehicleType,
            String manufacturer,
            String modelName,
            String licensePlate,
            Short passengerCapacity,
            Integer trunkVolumeLiter,
            Short maxLuggageCount,
            Short largeLuggageCapacity,
            String vehicleImageUrl
    ) {
        this.vehicleType = vehicleType;
        this.manufacturer = manufacturer;
        this.modelName = modelName;
        this.licensePlate = licensePlate;
        this.passengerCapacity = passengerCapacity;
        this.trunkVolumeLiter = trunkVolumeLiter;
        this.maxLuggageCount = maxLuggageCount;
        this.largeLuggageCapacity = largeLuggageCapacity;
        this.vehicleImageUrl = vehicleImageUrl;
    }

    public void deactivate() {
        this.active = false;
    }

}
