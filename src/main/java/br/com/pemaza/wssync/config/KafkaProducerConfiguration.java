package br.com.pemaza.wssync.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Configuration
public class KafkaProducerConfiguration {
    @Value("${kafka.consumer.address}")
    private String bootstrapServer;
    
    public ProducerFactory<String, KafkaMsgDTO> kafkaProducerFactory() {
        Map<String, Object> configs = getConfigs();
        return new DefaultKafkaProducerFactory<>(configs);
    }

    private ProducerFactory<String, KafkaGenericOutputDTO> kafkaGenericProducerFactory() {
        Map<String, Object> configs = getConfigs();
        return new DefaultKafkaProducerFactory<>(configs);
    }

    private Map<String, Object> getConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 3 minutos de retry
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 180000);
        // Retry a cada 5 segundos
        configs.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 5000);
        // 5 retries
        configs.put(ProducerConfig.RETRIES_CONFIG, 5);
        // 1 mensagem por vez
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return configs;
    }

    @Bean
    public KafkaTemplate<String, KafkaMsgDTO> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, KafkaGenericOutputDTO> kafkaGenericTemplate() {
        return new KafkaTemplate<>(kafkaGenericProducerFactory());
    }
}