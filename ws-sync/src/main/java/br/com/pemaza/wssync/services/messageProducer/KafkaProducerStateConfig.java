package br.com.pemaza.wssync.services.messageProducer;

import org.springframework.kafka.core.KafkaTemplate;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;
import br.com.pemaza.wssync.repositories.KafkaMsgBackupRepository;
import br.com.pemaza.wssync.repositories.KafkaOutputBackupRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class KafkaProducerStateConfig {
	String bootstrapServer;
	String listenerGroup;
	KafkaTemplate<String, KafkaGenericOutputDTO> template;
	KafkaTemplate<String, KafkaMsgDTO> msgTemplate;
	KafkaOutputBackupRepository outputRepository;
	KafkaMsgBackupRepository msgRepository;
	KafkaMessageProducer kafkaMessageProducer;
}
