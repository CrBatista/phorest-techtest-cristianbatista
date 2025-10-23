package com.phorest.client.infrastructure.repository.document;

public record ClientPayloadDocument(
        String clientId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        boolean banned
) {
}
