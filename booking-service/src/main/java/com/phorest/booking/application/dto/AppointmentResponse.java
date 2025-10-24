package com.phorest.booking.application.dto;

import java.time.Instant;

public record AppointmentResponse(
        String id,
        String clientId,
        Instant startTime,
        Instant endTime
) {
}
