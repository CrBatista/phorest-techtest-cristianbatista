package com.phorest.booking.application.events;

import com.phorest.booking.domain.serviceitem.ServiceEvent;

public interface ServiceEventPublisher {
    void publish(ServiceEvent event);
}
