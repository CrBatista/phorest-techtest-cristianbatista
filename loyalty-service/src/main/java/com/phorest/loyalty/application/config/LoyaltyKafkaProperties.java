package com.phorest.loyalty.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking.kafka")
public record LoyaltyKafkaProperties(Topics topic, boolean enabled) {
    public record Topics(String services, String purchases) {}
}
