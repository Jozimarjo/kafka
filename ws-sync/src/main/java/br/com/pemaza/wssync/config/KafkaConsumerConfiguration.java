package br.com.pemaza.wssync.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Configuration
@EnableKafka
public class KafkaConsumerConfiguration {

	@Value("${kafka.consumer.address}")
	private String bootstrapServer;
	
	@Value("${kafka.consumer.group}")
	private String listenerGroup;

	public ConsumerFactory<String, KafkaMsgDTO> kafkaConsumerFactory(){
		
		Map<String , Object>configs = new HashMap<>();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG	, bootstrapServer);
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, listenerGroup);
		configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
		configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		configs.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		configs.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
		configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, KafkaMsgDTO.class.getName());
		configs.put(JsonDeserializer.TRUSTED_PACKAGES, KafkaMsgDTO.class.getPackage().getName());
				
		return new DefaultKafkaConsumerFactory<>(configs);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, KafkaMsgDTO> kafkaListenerContainerFactory(){
		ConcurrentKafkaListenerContainerFactory<String, KafkaMsgDTO>factory=new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(kafkaConsumerFactory());
		factory.getContainerProperties().setMissingTopicsFatal(false);
		
		return factory;
	}

	public ConsumerFactory<String, KafkaGenericOutputDTO> kafkaGenericConsumerFactory() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG	, bootstrapServer);
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, listenerGroup);
		configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
		configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		configs.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		configs.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
		configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, KafkaGenericOutputDTO.class.getName());
		configs.put(JsonDeserializer.TRUSTED_PACKAGES, KafkaGenericOutputDTO.class.getPackage().getName());

		return new DefaultKafkaConsumerFactory<>(configs);
	}

	@Bean(name = "genericKafkaListener")
	public ConcurrentKafkaListenerContainerFactory<String, KafkaGenericOutputDTO> genericKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, KafkaGenericOutputDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(kafkaGenericConsumerFactory());
		factory.getContainerProperties().setMissingTopicsFatal(false);

		return factory;
	}
}
