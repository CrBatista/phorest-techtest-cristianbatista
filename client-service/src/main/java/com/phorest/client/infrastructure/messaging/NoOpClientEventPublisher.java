package com.phorest.client.infrastructure.messaging;

import com.phorest.client.application.events.ClientEventPublisher;
import com.phorest.client.domain.event.ClientEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "client.kafka.enabled", havingValue = "false")
public class NoOpClientEventPublisher implements ClientEventPublisher {
    @Override
    public void publish(ClientEvent event) {
        // no-op for tests or local runs without Kafka
    }
}
