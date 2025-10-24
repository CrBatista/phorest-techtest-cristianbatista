package com.phorest.loyalty.domain.serviceitem;

import java.util.List;

public interface ServiceEventStore {
    ServiceEvent append(ServiceEvent event);
    List<ServiceEvent> loadAll();
}
