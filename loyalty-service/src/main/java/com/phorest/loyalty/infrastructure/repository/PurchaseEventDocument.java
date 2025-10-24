package com.phorest.loyalty.infrastructure.repository;

import com.phorest.loyalty.messaging.PurchaseEventMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "loyalty_purchase_events")
public class PurchaseEventDocument {

    @Id
    private String eventId;
    private String purchaseId;
    private String type;
    private PurchaseEventMessage payload;
    private Instant occurredAt;
    private int version;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PurchaseEventMessage getPayload() {
        return payload;
    }

    public void setPayload(PurchaseEventMessage payload) {
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
