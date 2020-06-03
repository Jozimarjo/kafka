package br.com.pemaza.wssync.services.messageProducer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;
import br.com.pemaza.wssync.repositories.KafkaMsgBackupRepository;
import br.com.pemaza.wssync.repositories.KafkaOutputBackupRepository;

@Component
public class KafkaMessageProducer {
	private MessageProducerState state;
	@Value("${kafka.consumer.address}")
	private String bootstrapServer;
	
	@Value("${kafka.consumer.group}")
	private String listenerGroup;

	@Autowired
	KafkaTemplate<String, KafkaGenericOutputDTO> template;

	@Autowired
	KafkaTemplate<String, KafkaMsgDTO> msgTemplate;

	@Autowired(required = false)
	KafkaOutputBackupRepository outputRepository;

	@Autowired(required = false)
	KafkaMsgBackupRepository msgRepository;

	public void changeState(MessageProducerState state) {
		this.state = state;
	}

	@PostConstruct
	private void setUp() {
		KafkaProducerStateConfig kafkaProducerStateConfig= new KafkaProducerStateConfig(bootstrapServer, listenerGroup, template, msgTemplate, outputRepository, msgRepository, this);

		if (msgRepository == null) {
			changeState(new ActiveNoMessageState(kafkaProducerStateConfig));
		} else {
			ActiveStoredMessageState activeStored = new ActiveStoredMessageState(kafkaProducerStateConfig);	
			changeState(activeStored);
			activeStored.sendMessageMongo();
			
		}
	}

	public void sendMessage(KafkaMsgDTO msgDTO) {
		state.sendMessage(msgDTO);
	}

	public void sendOutputMessage(KafkaGenericOutputDTO outputDTO) {
		state.sendOutputMessage(outputDTO);
	}

}
