package com.phorest.loyalty.messaging;

import com.phorest.loyalty.application.config.LoyaltyKafkaProperties;
import com.phorest.loyalty.domain.purchase.PurchaseEvent;
import com.phorest.loyalty.domain.purchase.PurchaseEventStore;
import com.phorest.loyalty.domain.purchase.PurchaseEventType;
import com.phorest.loyalty.domain.purchase.PurchaseProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "client.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class PurchaseEventConsumer {

    private final PurchaseEventStore eventStore;
    private final Clock clock;
    private final LoyaltyKafkaProperties properties;

    public PurchaseEventConsumer(PurchaseEventStore eventStore, Clock clock, LoyaltyKafkaProperties properties) {
        this.eventStore = eventStore;
        this.clock = clock;
        this.properties = properties;
    }

    @KafkaListener(topics = "#{@loyaltyKafkaProperties.topic().purchases()}", groupId = "loyalty-service", containerFactory = "purchaseEventKafkaListenerContainerFactory")
    public void handlePurchaseEvent(PurchaseEventMessage message) {
        PurchaseProfile profile = new PurchaseProfile(
                message.purchaseId(),
                message.appointmentId(),
                message.clientId(),
                message.name(),
                message.price(),
                message.loyaltyPoints(),
                message.performedAt(),
                message.deleted()
        );
        PurchaseEvent event = new PurchaseEvent(
                message.eventId() != null ? message.eventId() : UUID.randomUUID().toString(),
                message.purchaseId(),
                PurchaseEventType.valueOf(message.eventType()),
                profile,
                message.occurredAt() != null ? message.occurredAt() : clock.instant(),
                message.version()
        );
        eventStore.append(event);
    }
}
