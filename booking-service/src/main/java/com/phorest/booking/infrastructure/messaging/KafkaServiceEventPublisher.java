package com.phorest.booking.infrastructure.messaging;

import com.phorest.booking.application.config.BookingKafkaProperties;
import com.phorest.booking.application.events.ServiceEventPublisher;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.messaging.ServiceEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaServiceEventPublisher implements ServiceEventPublisher {

    private final KafkaTemplate<String, ServiceEventMessage> kafkaTemplate;
    private final BookingKafkaProperties properties;

    public KafkaServiceEventPublisher(KafkaTemplate<String, ServiceEventMessage> kafkaTemplate,
                                      BookingKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(ServiceEvent event) {
        ServiceEventMessage message = new ServiceEventMessage(
                event.eventId(),
                event.serviceId(),
                event.payload().appointmentId(),
                event.payload().clientId(),
                event.payload().name(),
                event.payload().price(),
                event.payload().loyaltyPoints(),
                event.payload().performedAt(),
                event.type().name(),
                event.occurredAt(),
                event.version(),
                event.payload().deleted()
        );
        kafkaTemplate.send(properties.topic().services(), event.serviceId(), message);
    }
}
