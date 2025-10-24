package com.phorest.loyalty.messaging;

import com.phorest.loyalty.application.config.LoyaltyKafkaProperties;
import com.phorest.loyalty.domain.serviceitem.ServiceEvent;
import com.phorest.loyalty.domain.serviceitem.ServiceEventStore;
import com.phorest.loyalty.domain.serviceitem.ServiceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceEventConsumerTest {

    @Mock
    private ServiceEventStore eventStore;

    private ServiceEventConsumer consumer;

    @BeforeEach
    void setUp() {
        LoyaltyKafkaProperties properties = new LoyaltyKafkaProperties(new LoyaltyKafkaProperties.Topics("services", "purchases"), true);
        consumer = new ServiceEventConsumer(eventStore, Clock.systemUTC(), properties);
    }

    @Test
    void persistsIncomingEvent() {
        ServiceEventMessage message = new ServiceEventMessage(
                "evt-1",
                "service-1",
                "appointment-1",
                "client-1",
                "Colour",
                BigDecimal.TEN,
                10,
                Instant.parse("2024-01-01T10:00:00Z"),
                ServiceEventType.SERVICE_CREATED.name(),
                Instant.parse("2024-01-01T00:00:00Z"),
                1,
                false
        );

        consumer.handleServiceEvent(message);

        ArgumentCaptor<ServiceEvent> captor = ArgumentCaptor.forClass(ServiceEvent.class);
        verify(eventStore).append(captor.capture());
        assertThat(captor.getValue().serviceId()).isEqualTo("service-1");
    }
}
