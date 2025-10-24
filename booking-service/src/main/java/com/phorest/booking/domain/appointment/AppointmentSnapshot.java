package com.phorest.booking.domain.appointment;

import java.time.Instant;

public record AppointmentSnapshot(
        String appointmentId,
        String clientId,
        Instant startTime,
        Instant endTime,
        boolean deleted,
        int version
) {
}
