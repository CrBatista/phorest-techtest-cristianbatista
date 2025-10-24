package com.phorest.booking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public record AppointmentUpdateRequest(
        @NotBlank String clientId,
        @NotBlank String startTime,
        @NotBlank String endTime
) {
    public Instant parsedStartTime() {
        return Instant.parse(startTime);
    }

    public Instant parsedEndTime() {
        return Instant.parse(endTime);
    }
}
