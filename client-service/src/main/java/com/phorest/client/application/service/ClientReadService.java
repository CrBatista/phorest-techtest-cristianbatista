package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.domain.aggregate.ClientAggregate;
import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.model.ClientSnapshot;
import com.phorest.client.domain.repository.ClientEventStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientReadService {

    private final ClientEventStore eventStore;

    public ClientReadService(ClientEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Page<ClientResponse> getClients(Pageable pageable) {
        List<ClientEvent> events = eventStore.loadAll();

        Map<String, List<ClientEvent>> byClient = new LinkedHashMap<>();
        for (ClientEvent event : events) {
            byClient.computeIfAbsent(event.clientId(), key -> new java.util.ArrayList<>()).add(event);
        }

        List<ClientSnapshot> snapshots = byClient.values().stream()
                .map(ClientAggregate::fromEvents)
                .map(ClientAggregate::snapshot)
                .flatMap(Optional::stream)
                .filter(snapshot -> !snapshot.banned())
                .toList();

        int total = snapshots.size();
        int from = Math.min((int) pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        List<ClientResponse> content = snapshots.subList(from, to).stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    public Optional<ClientResponse> getClient(String clientId) {
        List<ClientEvent> events = eventStore.loadByClientId(clientId);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        ClientAggregate aggregate = ClientAggregate.fromEvents(events);
        return aggregate.snapshot()
                .filter(snapshot -> !snapshot.banned())
                .map(this::toResponse);
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
