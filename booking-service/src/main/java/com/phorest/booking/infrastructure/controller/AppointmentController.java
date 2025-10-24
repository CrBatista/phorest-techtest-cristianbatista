package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.AppointmentResponse;
import com.phorest.booking.application.dto.AppointmentUpdateRequest;
import com.phorest.booking.application.service.AppointmentCommandService;
import com.phorest.booking.application.service.AppointmentReadService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentReadService readService;
    private final AppointmentCommandService commandService;

    public AppointmentController(AppointmentReadService readService,
                                 AppointmentCommandService commandService) {
        this.readService = readService;
        this.commandService = commandService;
    }

    @GetMapping
    public Page<AppointmentResponse> list(Pageable pageable,
                                          @RequestParam(value = "clientId", required = false) String clientId) {
        return readService.getAppointments(pageable, clientId);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable("appointmentId") String appointmentId) {
        return readService.getAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable("appointmentId") String appointmentId,
                                                      @Validated @RequestBody AppointmentUpdateRequest request) {
        return commandService.updateAppointment(appointmentId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> delete(@PathVariable("appointmentId") String appointmentId) {
        return commandService.deleteAppointment(appointmentId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
