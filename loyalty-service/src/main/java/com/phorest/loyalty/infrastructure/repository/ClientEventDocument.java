package com.phorest.loyalty.infrastructure.repository;

import com.phorest.loyalty.messaging.ClientEventPayload;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "client_events")
public class ClientEventDocument {

    @Id
    private String eventId;
    private String clientId;
    private String type;
    private ClientEventPayload payload;
    private Instant occurredAt;
    private int version;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ClientEventPayload getPayload() {
        return payload;
    }

    public void setPayload(ClientEventPayload payload) {
        this.payload = payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
