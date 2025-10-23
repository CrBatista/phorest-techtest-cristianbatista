package com.phorest.booking.messaging;

import java.time.Instant;

public record ClientEventMessage(
        String eventId,
        String clientId,
        String type,
        ClientEventPayload payload,
        Instant occurredAt,
        int version
) {
}
