package com.phorest.booking.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseEventMessage(
        String eventId,
        String purchaseId,
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
