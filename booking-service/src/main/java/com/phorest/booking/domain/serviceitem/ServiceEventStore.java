package com.phorest.booking.domain.serviceitem;

import java.util.List;

public interface ServiceEventStore {
    List<ServiceEvent> loadByServiceId(String serviceId);
    ServiceEvent append(ServiceEvent event);
    List<ServiceEvent> loadAll();
}
