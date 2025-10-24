package com.phorest.loyalty.domain.purchase;

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
