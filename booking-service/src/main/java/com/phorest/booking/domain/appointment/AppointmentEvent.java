package com.phorest.booking.domain.appointment;

import java.time.Instant;

public record AppointmentEvent(
        String eventId,
        String appointmentId,
        AppointmentEventType type,
        AppointmentProfile payload,
        Instant occurredAt,
        int version
) {
}
