package com.phorest.booking.infrastructure.repository;

import com.phorest.booking.domain.serviceitem.ServiceEvent;
import com.phorest.booking.domain.serviceitem.ServiceEventStore;
import com.phorest.booking.domain.serviceitem.ServiceEventType;
import com.phorest.booking.domain.serviceitem.ServiceProfile;
import com.phorest.booking.infrastructure.repository.document.ServiceEventDocument;
import com.phorest.booking.infrastructure.repository.document.ServicePayloadDocument;
import com.phorest.booking.infrastructure.repository.spring.MongoServiceEventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoServiceEventStore implements ServiceEventStore {

    private final MongoServiceEventRepository repository;

    public MongoServiceEventStore(MongoServiceEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ServiceEvent> loadByServiceId(String serviceId) {
        return repository.findByServiceIdOrderByVersionAsc(serviceId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ServiceEvent append(ServiceEvent event) {
        ServiceEventDocument document = toDocument(event);
        ServiceEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    @Override
    public List<ServiceEvent> loadAll() {
        return repository.findAll(Sort.by(Sort.Order.asc("serviceId"), Sort.Order.asc("version")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ServiceEventDocument toDocument(ServiceEvent event) {
        ServiceEventDocument document = new ServiceEventDocument();
        document.setEventId(event.eventId());
        document.setServiceId(event.serviceId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());
        ServiceProfile payload = event.payload();
        document.setPayload(new ServicePayloadDocument(
                payload.serviceId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                payload.deleted()
        ));
        return document;
    }

    private ServiceEvent toDomain(ServiceEventDocument document) {
        ServicePayloadDocument payload = document.getPayload();
        ServiceProfile profile = new ServiceProfile(
                payload.serviceId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                payload.deleted()
        );
        return new ServiceEvent(
                document.getEventId(),
                document.getServiceId(),
                ServiceEventType.valueOf(document.getType()),
                profile,
                document.getOccurredAt(),
                document.getVersion()
        );
    }
}
