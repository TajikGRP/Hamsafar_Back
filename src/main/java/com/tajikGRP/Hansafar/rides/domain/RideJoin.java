package com.tajikGRP.Hansafar.rides.domain;

import com.tajikGRP.Hansafar.configuration.BaseEntity;
import com.tajikGRP.Hansafar.users.domain.Users;
import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ride_joins")
@AttributeOverride(
        name = "createdAt",
        column = @Column(name = "joined_at", nullable = false, updatable = false)
)
public class RideJoin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private RideSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Positive
    @Column(name = "passenger_count", nullable = false)
    private Short passengerCount;

    @PositiveOrZero
    @Column(name = "luggage_count", nullable = false)
    private Short luggageCount;

    @PositiveOrZero
    @Column(name = "large_luggage_count")
    private Short largeLuggageCount;

    @Column(name = "special_luggage_note", length = 500)
    private String specialLuggageNote;

    @PositiveOrZero
    @Column(name = "unit_fare_snapshot", nullable = false)
    private Long unitFareSnapshot;

    @PositiveOrZero
    @Column(name = "total_fare", nullable = false)
    private Long totalFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideJoinStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public LocalDateTime getJoinedAt() {
        return getCreatedAt();
    }

    public void approve() {
        if (this.status != RideJoinStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be approved.");
        }

        this.status = RideJoinStatus.APPROVED;
    }

    public void reject() {
        if (this.status != RideJoinStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be rejected.");
        }

        this.status = RideJoinStatus.REJECTED;
    }

    public boolean isApproved() {
        return this.status == RideJoinStatus.APPROVED;
    }
}
