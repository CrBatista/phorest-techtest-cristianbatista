package com.phorest.loyalty.infrastructure.controller;

import com.phorest.loyalty.application.dto.TopClientResponse;
import com.phorest.loyalty.application.service.TopClientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
public class LoyaltyController {

    private final TopClientService topClientService;

    public LoyaltyController(TopClientService topClientService) {
        this.topClientService = topClientService;
    }

    @GetMapping("/top-clients")
    public ResponseEntity<List<TopClientResponse>> topClients(@RequestParam(name = "limit", defaultValue = "10") int limit,
                                                              @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                              @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return ResponseEntity.ok(topClientService.topClients(limit, fromInstant, toInstant));
    }
}
