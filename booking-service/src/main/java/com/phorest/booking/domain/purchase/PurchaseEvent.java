package com.phorest.booking.domain.purchase;

import java.time.Instant;

public record PurchaseEvent(
        String eventId,
        String purchaseId,
        PurchaseEventType type,
        PurchaseProfile payload,
        Instant occurredAt,
        int version
) {
}
