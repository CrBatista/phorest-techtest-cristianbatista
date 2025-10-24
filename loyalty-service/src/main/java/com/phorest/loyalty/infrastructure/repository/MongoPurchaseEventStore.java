package com.phorest.loyalty.infrastructure.repository;

import com.phorest.loyalty.domain.purchase.PurchaseEvent;
import com.phorest.loyalty.domain.purchase.PurchaseEventStore;
import com.phorest.loyalty.domain.purchase.PurchaseEventType;
import com.phorest.loyalty.domain.purchase.PurchaseProfile;
import com.phorest.loyalty.messaging.PurchaseEventMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoPurchaseEventStore implements PurchaseEventStore {

    private final PurchaseEventMongoRepository repository;

    public MongoPurchaseEventStore(PurchaseEventMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public PurchaseEvent append(PurchaseEvent event) {
        PurchaseEventDocument document = toDocument(event);
        PurchaseEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    @Override
    public List<PurchaseEvent> loadAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private PurchaseEventDocument toDocument(PurchaseEvent event) {
        PurchaseEventDocument document = new PurchaseEventDocument();
        document.setEventId(event.eventId());
        document.setPurchaseId(event.purchaseId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());
        PurchaseProfile payload = event.payload();
        document.setPayload(new PurchaseEventMessage(
                event.eventId(),
                event.purchaseId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                event.type().name(),
                event.occurredAt(),
                event.version(),
                payload.deleted()
        ));
        return document;
    }

    private PurchaseEvent toDomain(PurchaseEventDocument document) {
        PurchaseEventMessage payload = document.getPayload();
        PurchaseProfile profile = new PurchaseProfile(
                payload.purchaseId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                payload.deleted()
        );
        return new PurchaseEvent(
                document.getEventId(),
                document.getPurchaseId(),
                PurchaseEventType.valueOf(document.getType()),
                profile,
                document.getOccurredAt(),
                document.getVersion()
        );
    }
}
