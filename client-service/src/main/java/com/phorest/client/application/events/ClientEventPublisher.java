package com.phorest.client.application.events;

import com.phorest.client.domain.event.ClientEvent;

public interface ClientEventPublisher {
    void publish(ClientEvent event);
}
