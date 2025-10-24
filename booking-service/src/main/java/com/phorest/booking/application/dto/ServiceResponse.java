package com.phorest.booking.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceResponse(
        String id,
        String appointmentId,
        String clientId,
        String name,
        BigDecimal price,
        int loyaltyPoints,
        Instant performedAt
) {
}
