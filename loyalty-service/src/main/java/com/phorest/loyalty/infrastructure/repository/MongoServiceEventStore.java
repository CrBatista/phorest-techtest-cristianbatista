package com.phorest.loyalty.infrastructure.repository;

import com.phorest.loyalty.domain.serviceitem.ServiceEvent;
import com.phorest.loyalty.domain.serviceitem.ServiceEventStore;
import com.phorest.loyalty.domain.serviceitem.ServiceEventType;
import com.phorest.loyalty.domain.serviceitem.ServiceProfile;
import com.phorest.loyalty.messaging.ServiceEventMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoServiceEventStore implements ServiceEventStore {

    private final ServiceEventMongoRepository repository;

    public MongoServiceEventStore(ServiceEventMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceEvent append(ServiceEvent event) {
        ServiceEventDocument document = toDocument(event);
        ServiceEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    @Override
    public List<ServiceEvent> loadAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private ServiceEventDocument toDocument(ServiceEvent event) {
        ServiceEventDocument document = new ServiceEventDocument();
        document.setEventId(event.eventId());
        document.setServiceId(event.serviceId());
        document.setType(event.type().name());
        document.setOccurredAt(event.occurredAt());
        document.setVersion(event.version());
        ServiceProfile payload = event.payload();
        document.setPayload(new ServiceEventMessage(
                event.eventId(),
                event.serviceId(),
                payload.appointmentId(),
                payload.clientId(),
                payload.name(),
                payload.price(),
                payload.loyaltyPoints(),
                payload.performedAt(),
                event.type().name(),
                event.occurredAt(),
                event.version(),
                payload.deleted()
        ));
        return document;
    }

    private ServiceEvent toDomain(ServiceEventDocument document) {
        ServiceEventMessage payload = document.getPayload();
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
