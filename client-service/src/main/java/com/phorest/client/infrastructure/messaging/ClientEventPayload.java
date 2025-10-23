package com.phorest.client.infrastructure.messaging;

public record ClientEventPayload(
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        boolean banned
) {
}
