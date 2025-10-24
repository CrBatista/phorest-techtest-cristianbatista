package com.phorest.loyalty.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceEventMessage(
        String eventId,
        String serviceId,
        String appointmentId,
        String clientId,
        String name,
        BigDecimal price,
        int loyaltyPoints,
        Instant performedAt,
        String eventType,
        Instant occurredAt,
        int version,
        boolean deleted
) {
}
