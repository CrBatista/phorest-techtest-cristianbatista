package com.phorest.booking.domain.purchase;

import java.util.List;
import java.util.Optional;

public class PurchaseAggregate {

    private final PurchaseSnapshot snapshot;
    private final int version;

    private PurchaseAggregate(PurchaseSnapshot snapshot, int version) {
        this.snapshot = snapshot;
        this.version = version;
    }

    public static PurchaseAggregate fromEvents(List<PurchaseEvent> events) {
        PurchaseSnapshot snapshot = null;
        int version = 0;
        for (PurchaseEvent event : events) {
            version = event.version();
            snapshot = applyEvent(snapshot, event);
        }
        return new PurchaseAggregate(snapshot, version);
    }

    private static PurchaseSnapshot applyEvent(PurchaseSnapshot current, PurchaseEvent event) {
        PurchaseProfile payload = event.payload();
        return new PurchaseSnapshot(
                payload.purchaseId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                payload.deleted(),
                event.version()
        );
    }

    public Optional<PurchaseSnapshot> snapshot() {
        return Optional.ofNullable(snapshot);
    }

    public Optional<PurchaseEventType> nextEventType(PurchaseProfile profile) {
        if (snapshot == null) {
            return Optional.of(PurchaseEventType.PURCHASE_CREATED);
        }
        if (profile.deleted()) {
            if (snapshot.deleted()) {
                return Optional.empty();
            }
            return Optional.of(PurchaseEventType.PURCHASE_DELETED);
        }
        if (snapshot.deleted()) {
            return Optional.of(PurchaseEventType.PURCHASE_UPDATED);
        }
        boolean unchanged = snapshot.appointmentId().equals(profile.appointmentId())
                && snapshot.clientId().equals(profile.clientId())
                && snapshot.name().equals(profile.name())
                && snapshot.price().compareTo(profile.price()) == 0
                && snapshot.loyaltyPoints() == profile.loyaltyPoints()
                && snapshot.performedAt().equals(profile.performedAt());
        if (unchanged) {
            return Optional.empty();
        }
        return Optional.of(PurchaseEventType.PURCHASE_UPDATED);
    }

    public int nextVersion() {
        return version + 1;
    }
}
