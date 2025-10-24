package com.phorest.booking.domain.appointment;

import java.util.List;
import java.util.Optional;

public class AppointmentAggregate {

    private final AppointmentSnapshot snapshot;
    private final int version;

    private AppointmentAggregate(AppointmentSnapshot snapshot, int version) {
        this.snapshot = snapshot;
        this.version = version;
    }

    public static AppointmentAggregate fromEvents(List<AppointmentEvent> events) {
        AppointmentSnapshot snapshot = null;
        int version = 0;
        for (AppointmentEvent event : events) {
            version = event.version();
            snapshot = applyEvent(snapshot, event);
        }
        return new AppointmentAggregate(snapshot, version);
    }

    private static AppointmentSnapshot applyEvent(AppointmentSnapshot current, AppointmentEvent event) {
        AppointmentProfile payload = event.payload();
        return new AppointmentSnapshot(
                payload.appointmentId(),
                payload.clientId(),
                payload.startTime(),
                payload.endTime(),
                payload.deleted(),
                event.version()
        );
    }

    public Optional<AppointmentSnapshot> snapshot() {
        return Optional.ofNullable(snapshot);
    }

    public Optional<AppointmentEventType> nextEventType(AppointmentProfile profile) {
        if (snapshot == null) {
            return Optional.of(AppointmentEventType.APPOINTMENT_REGISTERED);
        }
        if (profile.deleted()) {
            if (snapshot.deleted()) {
                return Optional.empty();
            }
            return Optional.of(AppointmentEventType.APPOINTMENT_DELETED);
        }
        if (snapshot.deleted()) {
            return Optional.of(AppointmentEventType.APPOINTMENT_UPDATED);
        }
        boolean unchanged = snapshot.clientId().equals(profile.clientId())
                && snapshot.startTime().equals(profile.startTime())
                && snapshot.endTime().equals(profile.endTime());
        if (unchanged) {
            return Optional.empty();
        }
        return Optional.of(AppointmentEventType.APPOINTMENT_UPDATED);
    }

    public int nextVersion() {
        return version + 1;
    }
}
