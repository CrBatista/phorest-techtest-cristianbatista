package com.phorest.loyalty.domain.serviceitem;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceSnapshot(
        String serviceId,
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
