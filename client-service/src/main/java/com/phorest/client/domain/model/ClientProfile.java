package com.phorest.client.domain.model;

public record ClientProfile(
        String clientId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        boolean banned
) {
}
