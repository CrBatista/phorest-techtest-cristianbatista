package com.phorest.booking.domain.appointment;

import java.time.Instant;

public record AppointmentProfile(
        String appointmentId,
        String clientId,
        Instant startTime,
        Instant endTime,
        boolean deleted
) {
}
