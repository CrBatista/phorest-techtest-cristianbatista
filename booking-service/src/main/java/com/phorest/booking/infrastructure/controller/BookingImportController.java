package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.service.AppointmentImportService;
import com.phorest.booking.application.service.PurchaseImportService;
import com.phorest.booking.application.service.ServiceImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
public class BookingImportController {

    private final AppointmentImportService appointmentImportService;
    private final ServiceImportService serviceImportService;
    private final PurchaseImportService purchaseImportService;

    public BookingImportController(AppointmentImportService appointmentImportService,
                                   ServiceImportService serviceImportService,
                                   PurchaseImportService purchaseImportService) {
        this.appointmentImportService = appointmentImportService;
        this.serviceImportService = serviceImportService;
        this.purchaseImportService = purchaseImportService;
    }

    @PostMapping(path = "/appointments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummary> importAppointments(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(appointmentImportService.importAppointments(file));
    }

    @PostMapping(path = "/services", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummary> importServices(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(serviceImportService.importServices(file));
    }

    @PostMapping(path = "/purchases", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummary> importPurchases(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(purchaseImportService.importPurchases(file));
    }
}
