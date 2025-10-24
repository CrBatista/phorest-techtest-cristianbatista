package com.phorest.booking.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking.kafka")
public record BookingKafkaProperties(Topics topic, boolean enabled) {
    public record Topics(String services, String purchases) {}
}
