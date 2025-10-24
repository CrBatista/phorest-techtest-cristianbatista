package com.phorest.booking.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceUpdateRequest(
        @NotBlank String appointmentId,
        @NotBlank String name,
        @NotNull BigDecimal price,
        @Min(0) int loyaltyPoints,
        @NotNull Instant performedAt
) {
}
