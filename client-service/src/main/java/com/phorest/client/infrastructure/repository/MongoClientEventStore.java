package com.phorest.client.infrastructure.repository;

import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.repository.ClientEventStore;
import com.phorest.client.infrastructure.repository.document.ClientEventDocument;
import com.phorest.client.infrastructure.repository.document.ClientPayloadDocument;
import com.phorest.client.infrastructure.repository.spring.MongoClientEventRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoClientEventStore implements ClientEventStore {

    private final MongoClientEventRepository repository;

    public MongoClientEventStore(MongoClientEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClientEvent> loadByClientId(String clientId) {
        return repository.findByClientIdOrderByVersionAsc(clientId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ClientEvent append(ClientEvent event) {
        ClientEventDocument document = toDocument(event);
        ClientEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    private ClientEvent toDomain(ClientEventDocument document) {
        ClientPayloadDocument payload = document.getPayload();
        ClientProfile profile = new ClientProfile(
                payload.clientId(),
                payload.firstName(),
                payload.lastName(),
                payload.email(),
                payload.phone(),
                payload.gender(),
                payload.banned()
        );

        return new ClientEvent(
                document.getEventId(),
                document.getClientId(),
                ClientEventType.valueOf(document.getType()),
                profile,
                document.getOccurredAt(),
                document.getVersion()
        );
    }

    private ClientEventDocument toDocument(ClientEvent event) {
        ClientEventDocument document = new ClientEventDocument();
        document.setEventId(event.eventId());
        document.setClientId(event.clientId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());

        ClientProfile payload = event.payload();
        document.setPayload(new ClientPayloadDocument(
                payload.clientId(),
                payload.firstName(),
                payload.lastName(),
                payload.email(),
                payload.phone(),
                payload.gender(),
                payload.banned()
        ));
        return document;
    }
}
