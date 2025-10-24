package com.phorest.loyalty.domain.serviceitem;

import java.util.List;
import java.util.Optional;

public class ServiceAggregate {

    private final ServiceSnapshot snapshot;
    private final int version;

    private ServiceAggregate(ServiceSnapshot snapshot, int version) {
        this.snapshot = snapshot;
        this.version = version;
    }

    public static ServiceAggregate fromEvents(List<ServiceEvent> events) {
        ServiceSnapshot snapshot = null;
        int version = 0;
        for (ServiceEvent event : events) {
            version = event.version();
            snapshot = applyEvent(snapshot, event);
        }
        return new ServiceAggregate(snapshot, version);
    }

    private static ServiceSnapshot applyEvent(ServiceSnapshot current, ServiceEvent event) {
        ServiceProfile payload = event.payload();
        return new ServiceSnapshot(
                payload.serviceId(),
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

    public Optional<ServiceSnapshot> snapshot() {
        return Optional.ofNullable(snapshot);
    }
}
