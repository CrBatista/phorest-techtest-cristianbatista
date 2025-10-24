package com.phorest.booking.domain.serviceitem;

import java.time.Instant;

public record ServiceEvent(
        String eventId,
        String serviceId,
        ServiceEventType type,
        ServiceProfile payload,
        Instant occurredAt,
        int version
) {
}
