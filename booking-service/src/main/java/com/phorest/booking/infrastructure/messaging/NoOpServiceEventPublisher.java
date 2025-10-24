package com.phorest.booking.infrastructure.messaging;

import com.phorest.booking.application.events.ServiceEventPublisher;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "false")
public class NoOpServiceEventPublisher implements ServiceEventPublisher {
    @Override
    public void publish(ServiceEvent event) {
        // no-op
    }
}
