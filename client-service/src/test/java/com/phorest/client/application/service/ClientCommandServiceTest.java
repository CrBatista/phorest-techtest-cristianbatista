package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.application.dto.ClientUpdateRequest;
import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.repository.ClientEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCommandServiceTest {

    @Mock
    private ClientEventStore eventStore;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private ClientCommandService service;

    @BeforeEach
    void setUp() {
        service = new ClientCommandService(eventStore, clock);
    }

    @Test
    void updateClientReturnsEmptyWhenClientMissing() {
        when(eventStore.loadByClientId("missing")).thenReturn(List.of());

        Optional<ClientResponse> response = service.updateClient("missing", request("Jane", "Doe", "jane@example.com"));

        assertThat(response).isEmpty();
        verify(eventStore, never()).append(any());
    }

    @Test
    void updateClientAppendsEventWhenDataChanges() {
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event("evt-1", profile("client-1", "Jane", "Doe", "jane@example.com", false), ClientEventType.CLIENT_REGISTERED, 1)
        ));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ClientResponse> response = service.updateClient("client-1", request("Jane", "Doe", "jane.new@example.com"));

        assertThat(response).isPresent();
        assertThat(response.get().email()).isEqualTo("jane.new@example.com");

        ArgumentCaptor<ClientEvent> eventCaptor = ArgumentCaptor.forClass(ClientEvent.class);
        verify(eventStore).append(eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ClientEventType.CLIENT_UPDATED);
    }

    @Test
    void banClientAppendsBanEvent() {
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event("evt-1", profile("client-1", "Jane", "Doe", "jane@example.com", false), ClientEventType.CLIENT_REGISTERED, 1)
        ));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.banClient("client-1");

        assertThat(result).isTrue();
        ArgumentCaptor<ClientEvent> eventCaptor = ArgumentCaptor.forClass(ClientEvent.class);
        verify(eventStore).append(eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ClientEventType.CLIENT_BANNED);
        assertThat(eventCaptor.getValue().payload().banned()).isTrue();
    }

    private ClientUpdateRequest request(String firstName, String lastName, String email) {
        return new ClientUpdateRequest(firstName, lastName, email, "1234567890", "Female");
    }

    private ClientEvent event(String eventId, ClientProfile profile, ClientEventType type, int version) {
        return new ClientEvent(eventId, profile.clientId(), type, profile, Instant.now(clock), version);
    }

    private ClientProfile profile(String id, String firstName, String lastName, String email, boolean banned) {
        return new ClientProfile(id, firstName, lastName, email, "1234567890", "Female", banned);
    }
}
