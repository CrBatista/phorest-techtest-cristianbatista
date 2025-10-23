package com.phorest.client.domain.aggregate;

import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAggregateTest {

    @Test
    void newAggregateRequestsRegisterEvent() {
        ClientAggregate aggregate = ClientAggregate.fromEvents(List.of());

        Optional<ClientEventType> nextType = aggregate.nextEventType(profile("client-1", "Jane", "Doe", "jane@example.com"));

        assertThat(nextType).contains(ClientEventType.CLIENT_REGISTERED);
        assertThat(aggregate.nextVersion()).isEqualTo(1);
    }

    @Test
    void unchangedProfileProducesNoEvent() {
        ClientProfile existing = profile("client-1", "Jane", "Doe", "jane@example.com");
        ClientEvent existingEvent = event("evt-1", existing, ClientEventType.CLIENT_REGISTERED, 1);
        ClientAggregate aggregate = ClientAggregate.fromEvents(List.of(existingEvent));

        Optional<ClientEventType> nextType = aggregate.nextEventType(existing);

        assertThat(nextType).isEmpty();
        assertThat(aggregate.nextVersion()).isEqualTo(2);
    }

    @Test
    void changedProfileProducesUpdateEvent() {
        ClientProfile existing = profile("client-1", "Jane", "Doe", "jane@example.com");
        ClientEvent existingEvent = event("evt-1", existing, ClientEventType.CLIENT_REGISTERED, 1);
        ClientAggregate aggregate = ClientAggregate.fromEvents(List.of(existingEvent));

        ClientProfile updated = profile("client-1", "Jane", "Doe", "jane.new@example.com");
        Optional<ClientEventType> nextType = aggregate.nextEventType(updated);

        assertThat(nextType).contains(ClientEventType.CLIENT_UPDATED);
    }

    private ClientProfile profile(String id, String firstName, String lastName, String email) {
        return new ClientProfile(id, firstName, lastName, email, "123", "Female", false);
    }

    private ClientEvent event(String eventId, ClientProfile payload, ClientEventType type, int version) {
        return new ClientEvent(eventId, payload.clientId(), type, payload, Instant.parse("2024-01-01T00:00:00Z"), version);
    }
}
