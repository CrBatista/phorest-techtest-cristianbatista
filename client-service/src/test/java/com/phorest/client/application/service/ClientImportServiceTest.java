package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientImportSummary;
import com.phorest.client.application.events.ClientEventPublisher;
import com.phorest.client.application.parser.ClientCsvParser;
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
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientImportServiceTest {

    @Mock
    private ClientCsvParser csvParser;

    @Mock
    private ClientEventStore eventStore;

    @Mock
    private ClientEventPublisher eventPublisher;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private ClientImportService service;

    @BeforeEach
    void setUp() {
        service = new ClientImportService(csvParser, eventStore, fixedClock, eventPublisher);
    }

    @Test
    void createsRegisterEventsForNewClients() {
        ClientProfile profile = profile("client-1", "Jane", "Doe");
        when(csvParser.parse(any())).thenReturn(List.of(profile));
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of());
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClientImportSummary summary = service.importClients(mockCsv());

        assertThat(summary).isEqualTo(new ClientImportSummary(1, 1, 0, 0));

        ArgumentCaptor<ClientEvent> captor = ArgumentCaptor.forClass(ClientEvent.class);
        verify(eventStore).append(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(ClientEventType.CLIENT_REGISTERED);
        assertThat(captor.getValue().occurredAt()).isEqualTo(Instant.now(fixedClock));
        verify(eventPublisher).publish(captor.getValue());
    }

    @Test
    void createsUpdateEventsWhenClientChanges() {
        ClientProfile profile = profile("client-1", "Jane", "Doe");
        ClientProfile updated = new ClientProfile("client-1", "Jane", "Doe", "jane.new@example.com", "123", "Female", false);

        when(csvParser.parse(any())).thenReturn(List.of(updated));
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event(profile, ClientEventType.CLIENT_REGISTERED, 1)
        ));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClientImportSummary summary = service.importClients(mockCsv());

        assertThat(summary).isEqualTo(new ClientImportSummary(1, 0, 1, 0));
        verify(eventStore, times(1)).append(any());
        verify(eventPublisher).publish(any());
    }

    @Test
    void skipsAppendWhenProfileUnchanged() {
        ClientProfile profile = profile("client-1", "Jane", "Doe");
        when(csvParser.parse(any())).thenReturn(List.of(profile));
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event(profile, ClientEventType.CLIENT_REGISTERED, 1)
        ));

        ClientImportSummary summary = service.importClients(mockCsv());

        assertThat(summary).isEqualTo(new ClientImportSummary(1, 0, 0, 1));
        verify(eventStore, never()).append(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void createsBanEventWhenClientMarkedBanned() {
        ClientProfile existing = profile("client-1", "Jane", "Doe");
        ClientProfile banned = new ClientProfile("client-1", "Jane", "Doe", "jane@example.com", "123", "Female", true);

        when(csvParser.parse(any())).thenReturn(List.of(banned));
        when(eventStore.loadByClientId("client-1")).thenReturn(List.of(
                event(existing, ClientEventType.CLIENT_REGISTERED, 1)
        ));
        when(eventStore.append(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClientImportSummary summary = service.importClients(mockCsv());

        assertThat(summary).isEqualTo(new ClientImportSummary(1, 0, 1, 0));
        ArgumentCaptor<ClientEvent> eventCaptor = ArgumentCaptor.forClass(ClientEvent.class);
        verify(eventStore).append(eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ClientEventType.CLIENT_BANNED);
        verify(eventPublisher).publish(eventCaptor.getValue());
    }

    private ClientProfile profile(String id, String firstName, String lastName) {
        return new ClientProfile(id, firstName, lastName, "jane@example.com", "123", "Female", false);
    }

    private ClientEvent event(ClientProfile payload, ClientEventType type, int version) {
        return new ClientEvent(UUID.randomUUID().toString(), payload.clientId(), type, payload, Instant.now(fixedClock), version);
    }

    private MockMultipartFile mockCsv() {
        return new MockMultipartFile("file", "clients.csv", "text/csv", "id,first_name".getBytes(StandardCharsets.UTF_8));
    }
}
