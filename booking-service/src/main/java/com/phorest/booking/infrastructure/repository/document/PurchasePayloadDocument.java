package com.phorest.booking.infrastructure.repository.document;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchasePayloadDocument(
        String purchaseId,
        String appointmentId,
        String clientId,
        String name,
        BigDecimal price,
        int loyaltyPoints,
        Instant performedAt,
        boolean deleted
) {
}
