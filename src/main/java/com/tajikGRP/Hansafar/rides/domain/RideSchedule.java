package com.tajikGRP.Hansafar.rides.domain;

import com.tajikGRP.Hansafar.configuration.BaseEntity;
import com.tajikGRP.Hansafar.drivers.domain.DriverProfile;
import com.tajikGRP.Hansafar.vehicles.domain.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ride_schedules")
public class RideSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Builder.Default
    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<RideJoin> rideJoins = new ArrayList<>();

    @Column(length = 200)
    private String title;

    @Column(name = "meeting_at", nullable = false)
    private LocalDateTime meetingAt;

    @Column(name = "join_deadline_at")
    private LocalDateTime joinDeadlineAt;

    @Column(name = "meeting_address", nullable = false, length = 500)
    private String meetingAddress;

    @Column(name = "meeting_place_name", length = 200)
    private String meetingPlaceName;

    @Column(name = "meeting_latitude", precision = 10, scale = 7)
    private BigDecimal meetingLatitude;

    @Column(name = "meeting_longitude", precision = 10, scale = 7)
    private BigDecimal meetingLongitude;

    @Column(name = "destination_address", length = 500)
    private String destinationAddress;

    @Column(name = "destination_place_name", length = 200)
    private String destinationPlaceName;

    @Column(name = "destination_latitude", precision = 10, scale = 7)
    private BigDecimal destinationLatitude;

    @Column(name = "destination_longitude", precision = 10, scale = 7)
    private BigDecimal destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "fare_type", nullable = false, length = 20)
    private FareType fareType;

    @Column(name = "fare_amount", nullable = false)
    private Long fareAmount;

    @Column(name = "max_passenger_count", nullable = false)
    private Short maxPassengerCount;

    @PositiveOrZero
    @Column(name = "max_luggage_count", nullable = false)
    private Short maxLuggageCount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideScheduleStatus status;
}
