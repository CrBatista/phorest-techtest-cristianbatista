package com.phorest.client.domain.event;

import com.phorest.client.domain.model.ClientProfile;

import java.time.Instant;

public record ClientEvent(
        String eventId,
        String clientId,
        ClientEventType type,
        ClientProfile payload,
        Instant occurredAt,
        int version
) {
}
