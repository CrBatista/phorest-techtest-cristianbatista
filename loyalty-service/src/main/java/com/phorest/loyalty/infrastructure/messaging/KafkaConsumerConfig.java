package com.phorest.loyalty.infrastructure.messaging;

import com.phorest.loyalty.messaging.PurchaseEventMessage;
import com.phorest.loyalty.messaging.ServiceEventMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "client.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ServiceEventMessage> serviceEventConsumerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildConsumerProperties();
        JsonDeserializer<ServiceEventMessage> deserializer = new JsonDeserializer<>(ServiceEventMessage.class, false);
        deserializer.addTrustedPackages("com.phorest.loyalty.messaging");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceEventMessage> serviceEventKafkaListenerContainerFactory(
            ConsumerFactory<String, ServiceEventMessage> serviceEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ServiceEventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(serviceEventConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PurchaseEventMessage> purchaseEventConsumerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildConsumerProperties();
        JsonDeserializer<PurchaseEventMessage> deserializer = new JsonDeserializer<>(PurchaseEventMessage.class, false);
        deserializer.addTrustedPackages("com.phorest.loyalty.messaging");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PurchaseEventMessage> purchaseEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PurchaseEventMessage> purchaseEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PurchaseEventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(purchaseEventConsumerFactory);
        return factory;
    }
}
