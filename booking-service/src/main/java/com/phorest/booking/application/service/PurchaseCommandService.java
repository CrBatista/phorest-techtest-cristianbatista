package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.PurchaseResponse;
import com.phorest.booking.application.dto.PurchaseUpdateRequest;
import com.phorest.booking.application.events.PurchaseEventPublisher;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.purchase.PurchaseAggregate;
import com.phorest.booking.domain.purchase.PurchaseEvent;
import com.phorest.booking.domain.purchase.PurchaseEventStore;
import com.phorest.booking.domain.purchase.PurchaseEventType;
import com.phorest.booking.domain.purchase.PurchaseProfile;
import com.phorest.booking.domain.purchase.PurchaseSnapshot;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PurchaseCommandService {

    private final PurchaseEventStore eventStore;
    private final AppointmentQueryService appointmentQueryService;
    private final PurchaseEventPublisher eventPublisher;
    private final Clock clock;

    public PurchaseCommandService(PurchaseEventStore eventStore,
                                  AppointmentQueryService appointmentQueryService,
                                  PurchaseEventPublisher eventPublisher,
                                  Clock clock) {
        this.eventStore = eventStore;
        this.appointmentQueryService = appointmentQueryService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public Optional<PurchaseResponse> updatePurchase(String purchaseId, PurchaseUpdateRequest request) {
        List<PurchaseEvent> events = eventStore.loadByPurchaseId(purchaseId);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        PurchaseAggregate aggregate = PurchaseAggregate.fromEvents(events);
        Optional<PurchaseSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().deleted()) {
            return Optional.empty();
        }

        AppointmentSnapshot appointment = appointmentQueryService.getSnapshot(request.appointmentId()).orElse(null);
        if (appointment == null || appointment.deleted()) {
            return Optional.empty();
        }

        PurchaseProfile profile = new PurchaseProfile(
                purchaseId,
                request.appointmentId(),
                appointment.clientId(),
                request.name(),
                request.price(),
                request.loyaltyPoints(),
                request.performedAt(),
                false
        );

        Optional<PurchaseEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return snapshotOpt.map(this::toResponse);
        }

        PurchaseEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        eventPublisher.publish(event);

        PurchaseSnapshot updated = new PurchaseSnapshot(
                profile.purchaseId(),
                profile.appointmentId(),
                profile.clientId(),
                profile.name(),
                profile.price(),
                profile.loyaltyPoints(),
                profile.performedAt(),
                false,
                event.version()
        );
        return Optional.of(toResponse(updated));
    }

    public boolean deletePurchase(String purchaseId) {
        List<PurchaseEvent> events = eventStore.loadByPurchaseId(purchaseId);
        if (events.isEmpty()) {
            return false;
        }
        PurchaseAggregate aggregate = PurchaseAggregate.fromEvents(events);
        Optional<PurchaseSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().deleted()) {
            return false;
        }

        PurchaseSnapshot snapshot = snapshotOpt.get();
        PurchaseProfile profile = new PurchaseProfile(
                snapshot.purchaseId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt(),
                true
        );

        Optional<PurchaseEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return false;
        }
        PurchaseEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        eventPublisher.publish(event);
        return true;
    }

    private PurchaseEvent buildEvent(PurchaseProfile profile, PurchaseEventType type, int version) {
        return new PurchaseEvent(
                UUID.randomUUID().toString(),
                profile.purchaseId(),
                type,
                profile,
                Instant.now(clock),
                version
        );
    }

    private PurchaseResponse toResponse(PurchaseSnapshot snapshot) {
        return new PurchaseResponse(
                snapshot.purchaseId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt()
        );
    }
}
