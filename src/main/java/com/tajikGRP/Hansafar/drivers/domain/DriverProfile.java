package com.tajikGRP.Hansafar.drivers.domain;

import com.tajikGRP.Hansafar.configuration.BaseEntity;
import com.tajikGRP.Hansafar.users.domain.Users;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "driver_profiles")
public class DriverProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @OneToOne(mappedBy = "driver", fetch = FetchType.LAZY)
    private Vehicle vehicle;

    @Column(name = "driver_phone", nullable = false, length = 30)
    private String driverPhone;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    public void update(
            String driverPhone,
            String introduction
    ) {
        this.driverPhone = driverPhone;
        this.introduction = introduction;
    }
}
