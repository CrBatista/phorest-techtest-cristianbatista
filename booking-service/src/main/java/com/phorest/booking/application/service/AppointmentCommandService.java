package com.phorest.booking.application.service;

import com.phorest.booking.application.dto.AppointmentResponse;
import com.phorest.booking.application.dto.AppointmentUpdateRequest;
import com.phorest.booking.domain.appointment.AppointmentAggregate;
import com.phorest.booking.domain.appointment.AppointmentEvent;
import com.phorest.booking.domain.appointment.AppointmentEventStore;
import com.phorest.booking.domain.appointment.AppointmentEventType;
import com.phorest.booking.domain.appointment.AppointmentProfile;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentCommandService {

    private final AppointmentEventStore eventStore;
    private final Clock clock;

    public AppointmentCommandService(AppointmentEventStore eventStore, Clock clock) {
        this.eventStore = eventStore;
        this.clock = clock;
    }

    public Optional<AppointmentResponse> updateAppointment(String appointmentId, AppointmentUpdateRequest request) {
        List<AppointmentEvent> events = eventStore.loadByAppointmentId(appointmentId);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        AppointmentAggregate aggregate = AppointmentAggregate.fromEvents(events);
        Optional<AppointmentSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty()) {
            return Optional.empty();
        }

        AppointmentSnapshot snapshot = snapshotOpt.get();
        if (snapshot.deleted()) {
            return Optional.empty();
        }

        AppointmentProfile profile = new AppointmentProfile(
                appointmentId,
                request.clientId(),
                request.parsedStartTime(),
                request.parsedEndTime(),
                false
        );

        Optional<AppointmentEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return Optional.of(toResponse(snapshot));
        }

        if (nextType.get() == AppointmentEventType.APPOINTMENT_DELETED) {
            // updating should not produce delete
            return Optional.empty();
        }

        AppointmentEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);

        AppointmentSnapshot updated = new AppointmentSnapshot(
                profile.appointmentId(),
                profile.clientId(),
                profile.startTime(),
                profile.endTime(),
                false,
                event.version()
        );
        return Optional.of(toResponse(updated));
    }

    public boolean deleteAppointment(String appointmentId) {
        List<AppointmentEvent> events = eventStore.loadByAppointmentId(appointmentId);
        if (events.isEmpty()) {
            return false;
        }
        AppointmentAggregate aggregate = AppointmentAggregate.fromEvents(events);
        Optional<AppointmentSnapshot> snapshotOpt = aggregate.snapshot();
        if (snapshotOpt.isEmpty() || snapshotOpt.get().deleted()) {
            return false;
        }

        AppointmentProfile profile = new AppointmentProfile(
                appointmentId,
                snapshotOpt.get().clientId(),
                snapshotOpt.get().startTime(),
                snapshotOpt.get().endTime(),
                true
        );

        Optional<AppointmentEventType> nextType = aggregate.nextEventType(profile);
        if (nextType.isEmpty()) {
            return false;
        }
        AppointmentEvent event = buildEvent(profile, nextType.get(), aggregate.nextVersion());
        eventStore.append(event);
        return true;
    }

    private AppointmentEvent buildEvent(AppointmentProfile profile, AppointmentEventType type, int version) {
        return new AppointmentEvent(
                UUID.randomUUID().toString(),
                profile.appointmentId(),
                type,
                profile,
                Instant.now(clock),
                version
        );
    }

    private AppointmentResponse toResponse(AppointmentSnapshot snapshot) {
        return new AppointmentResponse(
                snapshot.appointmentId(),
                snapshot.clientId(),
                snapshot.startTime(),
                snapshot.endTime()
        );
    }
}
