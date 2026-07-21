package com.tajikGRP.Hansafar.rides.repository;

import com.tajikGRP.Hansafar.rides.domain.RideJoinStatus;

public interface RideCapacitySummary {

    Long getScheduleId();

    RideJoinStatus getStatus();

    Long getPassengerCount();

    Long getLuggageCount();

    Long getRequestCount();
}
