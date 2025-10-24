package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.ServiceResponse;
import com.phorest.booking.application.dto.ServiceUpdateRequest;
import com.phorest.booking.application.service.ServiceCommandService;
import com.phorest.booking.application.service.ServiceReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceReadService readService;
    private final ServiceCommandService commandService;

    public ServiceController(ServiceReadService readService,
                             ServiceCommandService commandService) {
        this.readService = readService;
        this.commandService = commandService;
    }

    @GetMapping
    public Page<ServiceResponse> list(Pageable pageable) {
        return readService.getServices(pageable);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> get(@PathVariable("serviceId") String serviceId) {
        return readService.getService(serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> update(@PathVariable("serviceId") String serviceId,
                                                  @Validated @RequestBody ServiceUpdateRequest request) {
        return commandService.updateService(serviceId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> delete(@PathVariable("serviceId") String serviceId) {
        return commandService.deleteService(serviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
