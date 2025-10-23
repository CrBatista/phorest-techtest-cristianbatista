package com.phorest.client.domain.aggregate;

import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.model.ClientSnapshot;

import java.util.List;
import java.util.Optional;

public class ClientAggregate {

    private final ClientSnapshot snapshot;
    private final int version;

    private ClientAggregate(ClientSnapshot snapshot, int version) {
        this.snapshot = snapshot;
        this.version = version;
    }

    public static ClientAggregate fromEvents(List<ClientEvent> events) {
        ClientSnapshot snapshot = null;
        int version = 0;

        for (ClientEvent event : events) {
            version = event.version();
            snapshot = applyEvent(snapshot, event);
        }

        return new ClientAggregate(snapshot, version);
    }

    private static ClientSnapshot applyEvent(ClientSnapshot current, ClientEvent event) {
        ClientProfile payload = event.payload();
        return new ClientSnapshot(
                payload.clientId(),
                payload.firstName(),
                payload.lastName(),
                payload.email(),
                payload.phone(),
                payload.gender(),
                payload.banned(),
                event.version()
        );
    }

    public Optional<ClientSnapshot> snapshot() {
        return Optional.ofNullable(snapshot);
    }

    public Optional<ClientEventType> nextEventType(ClientProfile incomingProfile) {
        if (snapshot == null) {
            return Optional.of(ClientEventType.CLIENT_REGISTERED);
        }

        if (snapshot.matches(incomingProfile)) {
            return Optional.empty();
        }

        return Optional.of(ClientEventType.CLIENT_UPDATED);
    }

    public int nextVersion() {
        return version + 1;
    }
}
