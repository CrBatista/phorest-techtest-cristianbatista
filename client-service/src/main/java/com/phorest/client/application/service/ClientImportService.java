package com.phorest.client.application.service;

import com.phorest.client.application.dto.ClientImportSummary;
import com.phorest.client.application.events.ClientEventPublisher;
import com.phorest.client.application.parser.ClientCsvParser;
import com.phorest.client.domain.aggregate.ClientAggregate;
import com.phorest.client.domain.event.ClientEvent;
import com.phorest.client.domain.event.ClientEventType;
import com.phorest.client.domain.model.ClientProfile;
import com.phorest.client.domain.repository.ClientEventStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClientImportService {

    private final ClientCsvParser csvParser;
    private final ClientEventStore eventStore;
    private final Clock clock;
    private final ClientEventPublisher eventPublisher;

    public ClientImportService(ClientCsvParser csvParser,
                               ClientEventStore eventStore,
                               Clock clock,
                               ClientEventPublisher eventPublisher) {
        this.csvParser = csvParser;
        this.eventStore = eventStore;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    public ClientImportSummary importClients(MultipartFile file) {
        List<ClientProfile> profiles = parseProfiles(file);

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (ClientProfile profile : profiles) {
            ClientAggregate aggregate = ClientAggregate.fromEvents(eventStore.loadByClientId(profile.clientId()));
            var nextEventType = aggregate.nextEventType(profile);

            if (nextEventType.isEmpty()) {
                skipped++;
                continue;
            }

            ClientEvent event = buildEvent(profile, nextEventType.get(), aggregate.nextVersion());
            eventStore.append(event);
            eventPublisher.publish(event);

            if (nextEventType.get() == ClientEventType.CLIENT_REGISTERED) {
                created++;
            } else {
                updated++;
            }
        }

        return new ClientImportSummary(profiles.size(), created, updated, skipped);
    }

    private List<ClientProfile> parseProfiles(MultipartFile file) {
        try {
            return csvParser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read provided CSV file", e);
        }
    }

    private ClientEvent buildEvent(ClientProfile profile, ClientEventType eventType, int nextVersion) {
        Instant occurredAt = Instant.now(clock);
        return new ClientEvent(
                UUID.randomUUID().toString(),
                profile.clientId(),
                eventType,
                profile,
                occurredAt,
                nextVersion
        );
    }
}
