package com.phorest.loyalty.messaging;

import com.phorest.loyalty.domain.serviceitem.ServiceEvent;
import com.phorest.loyalty.domain.serviceitem.ServiceEventStore;
import com.phorest.loyalty.domain.serviceitem.ServiceEventType;
import com.phorest.loyalty.domain.serviceitem.ServiceProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceEventConsumer {

    private final ServiceEventStore eventStore;
    private final Clock clock;

    public ServiceEventConsumer(ServiceEventStore eventStore, Clock clock) {
        this.eventStore = eventStore;
        this.clock = clock;
    }

    @KafkaListener(topics = "${booking.kafka.topic.services:booking-services}", groupId = "loyalty-service", containerFactory = "serviceEventKafkaListenerContainerFactory")
    public void handleServiceEvent(ServiceEventMessage message) {
        ServiceProfile profile = new ServiceProfile(
                message.serviceId(),
                message.appointmentId(),
                message.clientId(),
                message.name(),
                message.price(),
                message.loyaltyPoints(),
                message.performedAt(),
                message.deleted()
        );
        ServiceEvent event = new ServiceEvent(
                message.eventId() != null ? message.eventId() : UUID.randomUUID().toString(),
                message.serviceId(),
                ServiceEventType.valueOf(message.eventType()),
                profile,
                message.occurredAt() != null ? message.occurredAt() : clock.instant(),
                message.version()
        );
        eventStore.append(event);
    }
}
