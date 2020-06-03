package br.com.pemaza.wssync.services.messageProducer;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

public abstract class MessageProducerState {
	protected KafkaProducerStateConfig kafkaProducerStateConfig;
	

	public MessageProducerState(KafkaProducerStateConfig kafkaProducerStateConfig) {
		this.kafkaProducerStateConfig = kafkaProducerStateConfig;
	}

	public abstract void sendMessage(KafkaMsgDTO msgDTO);

	public abstract void sendOutputMessage(KafkaGenericOutputDTO outputDTO);

}
