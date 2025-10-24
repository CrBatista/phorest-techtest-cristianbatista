package com.phorest.loyalty.infrastructure.messaging;

import com.phorest.loyalty.messaging.ClientEventMessage;
import com.phorest.loyalty.messaging.PurchaseEventMessage;
import com.phorest.loyalty.messaging.ServiceEventMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ServiceEventMessage> serviceEventConsumerFactory(KafkaProperties properties) {
        return jsonConsumerFactory(properties, ServiceEventMessage.class);
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
        return jsonConsumerFactory(properties, PurchaseEventMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PurchaseEventMessage> purchaseEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PurchaseEventMessage> purchaseEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PurchaseEventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(purchaseEventConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ClientEventMessage> clientEventConsumerFactory(KafkaProperties properties) {
        return jsonConsumerFactory(properties, ClientEventMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClientEventMessage> clientEventKafkaListenerContainerFactory(
            ConsumerFactory<String, ClientEventMessage> clientEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ClientEventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clientEventConsumerFactory);
        return factory;
    }

    private <T> ConsumerFactory<String, T> jsonConsumerFactory(KafkaProperties properties, Class<T> targetType) {
        Map<String, Object> props = properties.buildConsumerProperties();
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, false);
        deserializer.addTrustedPackages("com.phorest.loyalty.messaging");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
}
