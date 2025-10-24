package com.phorest.booking.infrastructure.messaging;

import com.phorest.booking.application.config.BookingKafkaProperties;
import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.messaging.PurchaseEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaPurchaseEventPublisher implements PurchaseEventPublisher {

    private final KafkaTemplate<String, PurchaseEventMessage> kafkaTemplate;
    private final BookingKafkaProperties properties;

    public KafkaPurchaseEventPublisher(KafkaTemplate<String, PurchaseEventMessage> kafkaTemplate,
                                       BookingKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(PurchaseEvent event) {
        PurchaseEventMessage message = new PurchaseEventMessage(
                event.eventId(),
                event.purchaseId(),
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
        kafkaTemplate.send(properties.topic().purchases(), event.purchaseId(), message);
    }
}
