package com.phorest.client.infrastructure.repository;

import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.infrastructure.repository.document.ClientEventDocument;
import com.phorest.client.infrastructure.repository.document.ClientPayloadDocument;
import com.phorest.client.infrastructure.repository.spring.MongoClientEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.*;

class MongoClientEventStoreTest {

    private final MongoClientEventRepository repository = mock(MongoClientEventRepository.class);
    private MongoClientEventStore store;

    @BeforeEach
    void setUp() {
        store = new MongoClientEventStore(repository);
    }

    @Test
    void loadsEventsOrderedByVersion() {
        ClientEventDocument v1 = document("evt-1", "client-1", "CLIENT_REGISTERED", 1, Instant.parse("2024-01-01T00:00:00Z"));
        ClientEventDocument v2 = document("evt-2", "client-1", "CLIENT_UPDATED", 2, Instant.parse("2024-01-02T00:00:00Z"));
        when(repository.findByClientIdOrderByVersionAsc("client-1")).thenReturn(List.of(v1, v2));

        List<ClientEvent> events = store.loadByClientId("client-1");

        assertThat(events)
                .hasSize(2)
                .extracting(ClientEvent::version, e -> e.type().name())
                .containsExactly(
                        tuple(1, "CLIENT_REGISTERED"),
                        tuple(2, "CLIENT_UPDATED")
                );
    }

    @Test
    void persistsDomainEventAsDocument() {
        ClientProfile payload = new ClientProfile("client-1", "Jane", "Doe", "jane@example.com", "123", "Female", false);
        ClientEvent event = new ClientEvent("evt-1", "client-1", ClientEventType.CLIENT_REGISTERED, payload, Instant.parse("2024-01-01T00:00:00Z"), 1);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClientEvent saved = store.append(event);

        ArgumentCaptor<ClientEventDocument> captor = ArgumentCaptor.forClass(ClientEventDocument.class);
        verify(repository).save(captor.capture());

        ClientEventDocument document = captor.getValue();
        assertThat(document.getEventId()).isEqualTo("evt-1");
        assertThat(document.getPayload().firstName()).isEqualTo("Jane");
        assertThat(saved.type()).isEqualTo(ClientEventType.CLIENT_REGISTERED);
    }

    private ClientEventDocument document(String eventId, String clientId, String type, int version, Instant happenedAt) {
        ClientEventDocument document = new ClientEventDocument();
        document.setEventId(eventId);
        document.setClientId(clientId);
        document.setType(type);
        document.setVersion(version);
        document.setOccurredAt(happenedAt);
        document.setPayload(new ClientPayloadDocument(clientId, "Jane", "Doe", "jane@example.com", "123", "Female", false));
        return document;
    }
}
