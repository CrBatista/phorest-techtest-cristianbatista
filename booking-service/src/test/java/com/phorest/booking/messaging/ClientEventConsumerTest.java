package com.phorest.booking.messaging;

import com.phorest.booking.infrastructure.repository.ClientEventDocument;
import com.phorest.booking.infrastructure.repository.ClientEventMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientEventConsumerTest {

    @Mock
    private ClientEventMongoRepository repository;

    @Test
    void storesIncomingEvent() {
        ClientEventConsumer consumer = new ClientEventConsumer(repository);
        ClientEventMessage message = new ClientEventMessage(
                "evt-1",
                "client-1",
                "CLIENT_UPDATED",
                new ClientEventPayload("Jane", "Doe", "jane@example.com", "123", "Female", false),
                Instant.parse("2024-01-01T00:00:00Z"),
                2
        );

        consumer.handleClientEvent(message);

        ArgumentCaptor<ClientEventDocument> captor = ArgumentCaptor.forClass(ClientEventDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getClientId()).isEqualTo("client-1");
        assertThat(captor.getValue().getType()).isEqualTo("CLIENT_UPDATED");
    }
}
