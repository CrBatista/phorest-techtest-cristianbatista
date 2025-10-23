package com.phorest.client.infrastructure.controller;

import com.phorest.client.application.dto.ClientImportSummary;
import com.phorest.client.application.service.ClientImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
public class ClientImportController {

    private final ClientImportService clientImportService;

    public ClientImportController(ClientImportService clientImportService) {
        this.clientImportService = clientImportService;
    }

    @PostMapping(path = "/clients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientImportSummary> importClients(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ClientImportSummary summary = clientImportService.importClients(file);
        return ResponseEntity.ok(summary);
    }
}
