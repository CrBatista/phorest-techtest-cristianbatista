package com.phorest.booking.infrastructure.repository;

import com.phorest.booking.domain.appointment.AppointmentEvent;
import com.phorest.booking.domain.appointment.AppointmentEventStore;
import com.phorest.booking.domain.appointment.AppointmentEventType;
import com.phorest.booking.domain.appointment.AppointmentProfile;
import com.phorest.booking.infrastructure.repository.document.AppointmentEventDocument;
import com.phorest.booking.infrastructure.repository.document.AppointmentPayloadDocument;
import com.phorest.booking.infrastructure.repository.spring.MongoAppointmentEventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoAppointmentEventStore implements AppointmentEventStore {

    private final MongoAppointmentEventRepository repository;

    public MongoAppointmentEventStore(MongoAppointmentEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AppointmentEvent> loadByAppointmentId(String appointmentId) {
        return repository.findByAppointmentIdOrderByVersionAsc(appointmentId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public AppointmentEvent append(AppointmentEvent event) {
        AppointmentEventDocument document = toDocument(event);
        AppointmentEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    @Override
    public List<AppointmentEvent> loadAll() {
        return repository.findAll(Sort.by(Sort.Order.asc("appointmentId"), Sort.Order.asc("version")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AppointmentEventDocument toDocument(AppointmentEvent event) {
        AppointmentEventDocument document = new AppointmentEventDocument();
        document.setEventId(event.eventId());
        document.setAppointmentId(event.appointmentId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());
        AppointmentProfile payload = event.payload();
        document.setPayload(new AppointmentPayloadDocument(
                payload.appointmentId(),
                payload.clientId(),
                payload.startTime(),
                payload.endTime(),
                payload.deleted()
        ));
        return document;
    }

    private AppointmentEvent toDomain(AppointmentEventDocument document) {
        AppointmentPayloadDocument payload = document.getPayload();
        AppointmentProfile profile = new AppointmentProfile(
                payload.appointmentId(),
                payload.clientId(),
                payload.startTime(),
                payload.endTime(),
                payload.deleted()
        );
        return new AppointmentEvent(
                document.getEventId(),
                document.getAppointmentId(),
                AppointmentEventType.valueOf(document.getType()),
                profile,
                document.getOccurredAt(),
                document.getVersion()
        );
    }
}
