package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.repository.ClientEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientReadServiceTest {

    @Mock
    private ClientEventStore eventStore;

    private ClientReadService service;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new ClientReadService(eventStore);
    }

    @Test
    void getClientsReturnsOnlyNonBannedClients() {
        when(eventStore.loadAll()).thenReturn(List.of(
                event("client-1", ClientEventType.CLIENT_REGISTERED, false, 1),
                event("client-2", ClientEventType.CLIENT_REGISTERED, false, 1),
                event("client-2", ClientEventType.CLIENT_BANNED, true, 2)
        ));

        Page<ClientResponse> page = service.getClients(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(ClientResponse::id).containsExactly("client-1");
    }

    @Test
    void getClientReturnsEmptyForBannedClient() {
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event("client-1", ClientEventType.CLIENT_REGISTERED, false, 1),
                event("client-1", ClientEventType.CLIENT_BANNED, true, 2)
        ));

        Optional<ClientResponse> response = service.getClient("client-1");

        assertThat(response).isEmpty();
    }

    private ClientEvent event(String clientId, ClientEventType type, boolean banned, int version) {
        ClientProfile profile = new ClientProfile(
                clientId,
                "Jane",
                "Doe",
                clientId + "@example.com",
                "123",
                "Female",
                banned
        );
        return new ClientEvent(
                clientId + "-evt-" + version,
                clientId,
                type,
                profile,
                Instant.now(clock).plusSeconds(version),
                version
        );
    }
}
