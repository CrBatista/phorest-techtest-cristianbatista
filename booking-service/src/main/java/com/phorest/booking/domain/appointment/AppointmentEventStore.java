package com.phorest.booking.domain.appointment;

import java.util.List;

public interface AppointmentEventStore {
    List<AppointmentEvent> loadByAppointmentId(String appointmentId);
    AppointmentEvent append(AppointmentEvent event);
    List<AppointmentEvent> loadAll();
}
