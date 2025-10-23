package com.phorest.client.domain.model;

public record ClientSnapshot(
        String clientId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        boolean banned,
        int version
) {

    public boolean matches(ClientProfile profile) {
        return profile.firstName().equals(firstName)
                && profile.lastName().equals(lastName)
                && profile.email().equals(email)
                && profile.phone().equals(phone)
                && profile.gender().equals(gender)
                && profile.banned() == banned;
    }
}
