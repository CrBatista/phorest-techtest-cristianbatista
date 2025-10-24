package com.phorest.booking.infrastructure.messaging;

import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "false")
public class NoOpPurchaseEventPublisher implements PurchaseEventPublisher {
    @Override
    public void publish(PurchaseEvent event) {
        // no-op
    }
}
