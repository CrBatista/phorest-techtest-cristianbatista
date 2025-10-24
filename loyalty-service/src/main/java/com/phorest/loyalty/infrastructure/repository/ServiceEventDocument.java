package com.phorest.loyalty.infrastructure.repository;

import com.phorest.loyalty.messaging.ServiceEventMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "loyalty_service_events")
public class ServiceEventDocument {

    @Id
    private String eventId;
    private String serviceId;
    private String type;
    private ServiceEventMessage payload;
    private Instant occurredAt;
    private int version;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ServiceEventMessage getPayload() {
        return payload;
    }

    public void setPayload(ServiceEventMessage payload) {
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
