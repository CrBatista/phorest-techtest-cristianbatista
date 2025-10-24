package com.phorest.booking.infrastructure.messaging;

import com.phorest.booking.messaging.PurchaseEventMessage;
import com.phorest.booking.messaging.ServiceEventMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "booking.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, ServiceEventMessage> serviceEventProducerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, PurchaseEventMessage> purchaseEventProducerFactory(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ServiceEventMessage> serviceEventKafkaTemplate(
            ProducerFactory<String, ServiceEventMessage> serviceEventProducerFactory) {
        return new KafkaTemplate<>(serviceEventProducerFactory);
    }

    @Bean
    public KafkaTemplate<String, PurchaseEventMessage> purchaseEventKafkaTemplate(
            ProducerFactory<String, PurchaseEventMessage> purchaseEventProducerFactory) {
        return new KafkaTemplate<>(purchaseEventProducerFactory);
    }
}
