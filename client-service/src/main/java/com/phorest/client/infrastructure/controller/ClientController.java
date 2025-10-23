package com.phorest.client.infrastructure.controller;

import com.phorest.client.application.dto.ClientResponse;
import com.phorest.client.application.dto.ClientUpdateRequest;
import com.phorest.client.application.service.ClientCommandService;
import com.phorest.client.application.service.ClientReadService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientReadService readService;
    private final ClientCommandService commandService;

    public ClientController(ClientReadService readService, ClientCommandService commandService) {
        this.readService = readService;
        this.commandService = commandService;
    }

    @GetMapping
    public Page<ClientResponse> getClients(Pageable pageable) {
        return readService.getClients(pageable);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable("clientId") String clientId) {
        return readService.getClient(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable("clientId") String clientId,
                                                       @Valid @RequestBody ClientUpdateRequest request) {
        return commandService.updateClient(clientId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> banClient(@PathVariable("clientId") String clientId) {
        boolean banned = commandService.banClient(clientId);
        return banned ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
