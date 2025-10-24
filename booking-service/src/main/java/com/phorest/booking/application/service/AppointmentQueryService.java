package com.phorest.booking.application.service;

import com.phorest.booking.domain.appointment.AppointmentAggregate;
import com.phorest.booking.domain.appointment.AppointmentEventStore;
import com.phorest.booking.domain.appointment.AppointmentSnapshot;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppointmentQueryService {

    private final AppointmentEventStore eventStore;

    public AppointmentQueryService(AppointmentEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Optional<AppointmentSnapshot> getSnapshot(String appointmentId) {
        return AppointmentAggregate.fromEvents(eventStore.loadByAppointmentId(appointmentId)).snapshot();
    }
}
