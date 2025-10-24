package com.phorest.booking.domain.serviceitem;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceProfile(
        String serviceId,
        String appointmentId,
        String clientId,
        String name,
        BigDecimal price,
        int loyaltyPoints,
        Instant performedAt,
        boolean deleted
) {
}
