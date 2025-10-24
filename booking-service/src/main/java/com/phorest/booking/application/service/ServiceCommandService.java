package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.ServiceResponse;
import com.phorest.booking.application.dto.ServiceUpdateRequest;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import com.phorest.booking.domain.serviceitem.ServiceAggregate;
import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceEventType;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
import com.phorest.booking.domain.serviceitem.ServiceSnapshot;
import com.phorest.booking.application.events.ServiceEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceCommandService {

    private final ServiceEventStore eventStore;
    private final AppointmentQueryService appointmentQueryService;
    private final ServiceEventPublisher eventPublisher;
    private final Clock clock;

    public ServiceCommandService(ServiceEventStore eventStore,
                                 AppointmentQueryService appointmentQueryService,
                                 ServiceEventPublisher eventPublisher,
                                 Clock clock) {
        this.eventStore = eventStore;
        this.appointmentQueryService = appointmentQueryService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public Optional<ServiceResponse> updateService(String serviceId, ServiceUpdateRequest request) {
        List<ServiceEvent> events = eventStore.loadByServiceId(serviceId);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        ServiceAggregate aggregate = ServiceAggregate.fromEvents(events);
        Optional<ServiceSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().deleted()) {
            return Optional.empty();
        }

        AppointmentSnapshot appointment = appointmentQueryService.getSnapshot(request.appointmentId()).orElse(null);
        if (appointment == null || appointment.deleted()) {
            return Optional.empty();
        }

        ServiceProfile profile = new ServiceProfile(
                serviceId,
                request.appointmentId(),
                appointment.clientId(),
                request.name(),
                request.price(),
                request.loyaltyPoints(),
                request.performedAt(),
                false
        );

        Optional<ServiceEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return snapshotOpt.map(this::toResponse);
        }

        ServiceEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        eventPublisher.publish(event);

        ServiceSnapshot updated = new ServiceSnapshot(
                profile.serviceId(),
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

    public boolean deleteService(String serviceId) {
        List<ServiceEvent> events = eventStore.loadByServiceId(serviceId);
        if (events.isEmpty()) {
            return false;
        }
        ServiceAggregate aggregate = ServiceAggregate.fromEvents(events);
        Optional<ServiceSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().deleted()) {
            return false;
        }

        ServiceSnapshot snapshot = snapshotOpt.get();
        ServiceProfile profile = new ServiceProfile(
                snapshot.serviceId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt(),
                true
        );

        Optional<ServiceEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return false;
        }
        ServiceEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        eventPublisher.publish(event);
        return true;
    }

    private ServiceEvent buildEvent(ServiceProfile profile, ServiceEventType type, int version) {
        return new ServiceEvent(
                UUID.randomUUID().toString(),
                profile.serviceId(),
                type,
                profile,
                Instant.now(clock),
                version
        );
    }

    private ServiceResponse toResponse(ServiceSnapshot snapshot) {
        return new ServiceResponse(
                snapshot.serviceId(),
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.name(),
                snapshot.price(),
                snapshot.loyaltyPoints(),
                snapshot.performedAt()
        );
    }
}
