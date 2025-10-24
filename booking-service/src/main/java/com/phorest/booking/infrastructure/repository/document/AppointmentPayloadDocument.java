package com.phorest.booking.infrastructure.repository.document;

import java.time.Instant;

public record AppointmentPayloadDocument(
        String appointmentId,
        String clientId,
        Instant startTime,
        Instant endTime,
        boolean deleted
) {
}
