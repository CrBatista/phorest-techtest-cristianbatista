package com.phorest.loyalty.messaging;

import com.phorest.loyalty.application.config.LoyaltyKafkaProperties;
import com.phorest.loyalty.domain.purchase.PurchaseEvent;
import com.phorest.loyalty.domain.purchase.PurchaseEventStore;
import com.phorest.loyalty.domain.purchase.PurchaseEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseEventConsumerTest {

    @Mock
    private PurchaseEventStore eventStore;

    private PurchaseEventConsumer consumer;

    @BeforeEach
    void setUp() {
        LoyaltyKafkaProperties properties = new LoyaltyKafkaProperties(new LoyaltyKafkaProperties.Topics("services", "purchases"), true);
        consumer = new PurchaseEventConsumer(eventStore, Clock.systemUTC(), properties);
    }

    @Test
    void persistsIncomingEvent() {
        PurchaseEventMessage message = new PurchaseEventMessage(
                "evt-1",
                "purchase-1",
                "appointment-1",
                "client-1",
                "Shampoo",
                BigDecimal.TEN,
                10,
                Instant.parse("2024-01-01T10:00:00Z"),
                PurchaseEventType.PURCHASE_CREATED.name(),
                Instant.parse("2024-01-01T00:00:00Z"),
                1,
                false
        );

        consumer.handlePurchaseEvent(message);

        ArgumentCaptor<PurchaseEvent> captor = ArgumentCaptor.forClass(PurchaseEvent.class);
        verify(eventStore).append(captor.capture());
        assertThat(captor.getValue().purchaseId()).isEqualTo("purchase-1");
    }
}
