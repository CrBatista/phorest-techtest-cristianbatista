package com.phorest.client.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.kafka")
public record ClientKafkaProperties(String topic, boolean enabled) {
}
