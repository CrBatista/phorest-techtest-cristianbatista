package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.application.dto.ClientUpdateRequest;
import com.phorest.client.domain.aggregate.ClientAggregate;
import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.model.ClientSnapshot;
import com.phorest.client.domain.repository.ClientEventStore;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientCommandService {

    private final ClientEventStore eventStore;
    private final Clock clock;

    public ClientCommandService(ClientEventStore eventStore, Clock clock) {
        this.eventStore = eventStore;
        this.clock = clock;
    }

    public Optional<ClientResponse> updateClient(String clientId, ClientUpdateRequest request) {
        List<ClientEvent> events = eventStore.loadByClientId(clientId);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        ClientAggregate aggregate = ClientAggregate.fromEvents(events);
        Optional<ClientSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().banned()) {
            return Optional.empty();
        }

        ClientProfile profile = new ClientProfile(
                clientId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.gender(),
                false
        );

        Optional<ClientEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return snapshotOpt.map(this::toResponse);
        }

        if (nextType.get() == ClientEventType.CLIENT_BANNED) {
            return Optional.empty();
        }

        ClientEvent event = buildEvent(clientId, profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);

        ClientSnapshot updatedSnapshot = new ClientSnapshot(
                profile.clientId(),
                profile.firstName(),
                profile.lastName(),
                profile.email(),
                profile.phone(),
                profile.gender(),
                profile.banned(),
                event.version()
        );
        return Optional.of(toResponse(updatedSnapshot));
    }

    public boolean banClient(String clientId) {
        List<ClientEvent> events = eventStore.loadByClientId(clientId);
        if (events.isEmpty()) {
            return false;
        }

        ClientAggregate aggregate = ClientAggregate.fromEvents(events);
        Optional<ClientSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().banned()) {
            return false;
        }

        ClientSnapshot snapshot = snapshotOpt.get();
        ClientProfile bannedProfile = new ClientProfile(
                snapshot.clientId(),
                snapshot.firstName(),
                snapshot.lastName(),
                snapshot.email(),
                snapshot.phone(),
                snapshot.gender(),
                true
        );

        Optional<ClientEventType> nextType = aggregate.nextEventType(bannedProfile);
        if (nextType.isEmpty() || nextType.get() != ClientEventType.CLIENT_BANNED) {
            return false;
        }

        ClientEvent event = buildEvent(snapshot.clientId(), bannedProfile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        return true;
    }

    private ClientEvent buildEvent(String clientId, ClientProfile profile, ClientEventType type, int version) {
        return new ClientEvent(
                UUID.randomUUID().toString(),
                clientId,
                type,
                profile,
                Instant.now(clock),
                version
        );
    }

    private ClientResponse toResponse(ClientSnapshot snapshot) {
        return new ClientResponse(
                snapshot.clientId(),
                snapshot.firstName(),
                snapshot.lastName(),
                snapshot.email(),
                snapshot.phone(),
                snapshot.gender()
        );
    }
}
