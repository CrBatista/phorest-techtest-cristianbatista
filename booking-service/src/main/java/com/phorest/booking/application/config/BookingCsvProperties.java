package com.phorest.booking.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "booking.csv")
public record BookingCsvProperties(
        List<String> appointmentsHeaders,
        List<String> servicesHeaders,
        List<String> purchasesHeaders
) {
}
