package com.phorest.client.infrastructure.messaging;

import com.phorest.client.application.config.ClientKafkaProperties;
import com.phorest.client.application.events.ClientEventPublisher;
import com.phorest.client.domain.event.ClientEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "client.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaClientEventPublisher implements ClientEventPublisher {

    private final KafkaTemplate<String, ClientEventMessage> kafkaTemplate;
    private final ClientKafkaProperties properties;

    public KafkaClientEventPublisher(KafkaTemplate<String, ClientEventMessage> kafkaTemplate,
                                     ClientKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(ClientEvent event) {
        ClientEventPayload payload = new ClientEventPayload(
                event.payload().firstName(),
                event.payload().lastName(),
                event.payload().email(),
                event.payload().phone(),
                event.payload().gender(),
                event.payload().banned()
        );

        ClientEventMessage message = new ClientEventMessage(
                event.eventId(),
                event.clientId(),
                event.type().name(),
                payload,
                event.occurredAt(),
                event.version()
        );

        kafkaTemplate.send(properties.topic(), event.clientId(), message);
    }
}
