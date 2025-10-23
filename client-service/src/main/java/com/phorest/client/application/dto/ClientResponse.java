package com.phorest.client.application.dto;

public record ClientResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender
) {
}
