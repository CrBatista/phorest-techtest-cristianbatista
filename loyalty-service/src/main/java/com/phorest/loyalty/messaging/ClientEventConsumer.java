package com.phorest.loyalty.messaging;

import com.phorest.loyalty.infrastructure.repository.ClientEventDocument;
import com.phorest.loyalty.infrastructure.repository.ClientEventMongoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "client.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ClientEventConsumer {

    private final ClientEventMongoRepository repository;

    public ClientEventConsumer(ClientEventMongoRepository repository) {
        this.repository = repository;
    }

@KafkaListener(topics = "${client.kafka.topic}", groupId = "loyalty-service", containerFactory = "clientEventKafkaListenerContainerFactory")
    public void handleClientEvent(ClientEventMessage message) {
        ClientEventDocument document = new ClientEventDocument();
        document.setEventId(message.eventId());
        document.setClientId(message.clientId());
        document.setType(message.type());
        document.setOccurredAt(message.occurredAt());
        document.setVersion(message.version());
        document.setPayload(message.payload());
        repository.save(document);
    }
}
