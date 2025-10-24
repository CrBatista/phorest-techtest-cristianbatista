package com.phorest.booking.domain.serviceitem;

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

    public Optional<ServiceEventType> nextEventType(ServiceProfile profile) {
        if (snapshot == null) {
            return Optional.of(ServiceEventType.SERVICE_CREATED);
        }
        if (profile.deleted()) {
            if (snapshot.deleted()) {
                return Optional.empty();
            }
            return Optional.of(ServiceEventType.SERVICE_DELETED);
        }
        if (snapshot.deleted()) {
            return Optional.of(ServiceEventType.SERVICE_UPDATED);
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
        return Optional.of(ServiceEventType.SERVICE_UPDATED);
    }

    public int nextVersion() {
        return version + 1;
    }
}
