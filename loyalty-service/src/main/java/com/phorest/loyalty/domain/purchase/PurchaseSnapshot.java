package com.phorest.loyalty.domain.purchase;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseSnapshot(
        String purchaseId,
        String appointmentId,
        String clientId,
        String name,
        BigDecimal price,
        int loyaltyPoints,
        Instant performedAt,
        boolean deleted,
        int version
) {
}
