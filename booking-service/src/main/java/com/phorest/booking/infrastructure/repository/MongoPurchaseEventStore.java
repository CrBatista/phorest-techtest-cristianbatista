package com.phorest.booking.infrastructure.repository;

import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseEventType;
import com.phorest.booking.domain.purchase.PurchaseProfile;
import com.phorest.booking.infrastructure.repository.document.PurchaseEventDocument;
import com.phorest.booking.infrastructure.repository.document.PurchasePayloadDocument;
import com.phorest.booking.infrastructure.repository.spring.MongoPurchaseEventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoPurchaseEventStore implements PurchaseEventStore {

    private final MongoPurchaseEventRepository repository;

    public MongoPurchaseEventStore(MongoPurchaseEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PurchaseEvent> loadByPurchaseId(String purchaseId) {
        return repository.findByPurchaseIdOrderByVersionAsc(purchaseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PurchaseEvent append(PurchaseEvent event) {
        PurchaseEventDocument document = toDocument(event);
        PurchaseEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    @Override
    public List<PurchaseEvent> loadAll() {
        return repository.findAll(Sort.by(Sort.Order.asc("purchaseId"), Sort.Order.asc("version")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PurchaseEventDocument toDocument(PurchaseEvent event) {
        PurchaseEventDocument document = new PurchaseEventDocument();
        document.setEventId(event.eventId());
        document.setPurchaseId(event.purchaseId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());
        PurchaseProfile payload = event.payload();
        document.setPayload(new PurchasePayloadDocument(
                payload.purchaseId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                payload.deleted()
        ));
        return document;
    }

    private PurchaseEvent toDomain(PurchaseEventDocument document) {
        PurchasePayloadDocument payload = document.getPayload();
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
