package com.phorest.client.domain.repository;

import com.phorest.client.domain.event.ClientEvent;

import java.util.List;

public interface ClientEventStore {

    List<ClientEvent> loadByClientId(String clientId);

    ClientEvent append(ClientEvent event);
}
