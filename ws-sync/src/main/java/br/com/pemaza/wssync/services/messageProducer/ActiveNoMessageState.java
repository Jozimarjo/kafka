package br.com.pemaza.wssync.services.messageProducer;

import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.kafka.support.SendResult;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

public class ActiveNoMessageState extends MessageProducerState {

	

	public ActiveNoMessageState(KafkaProducerStateConfig kafkaProducerStateConfig) {
		super(kafkaProducerStateConfig);
	}

	@Override
	public void sendMessage(KafkaMsgDTO msgDTO) {
		kafkaProducerStateConfig.getMsgTemplate().send(msgDTO.getTopic(), msgDTO).addCallback(result -> {
		}, exception -> {
			if (exception.getCause() instanceof TimeoutException && kafkaProducerStateConfig.getMsgRepository() != null) {
				InactiveStoredMessageState inactiveStoredMessageState = new InactiveStoredMessageState(kafkaProducerStateConfig);
				kafkaProducerStateConfig.getKafkaMessageProducer().changeState(inactiveStoredMessageState);
				inactiveStoredMessageState.sendMessage(msgDTO);
			}
		});

	}

	@Override
	public void sendOutputMessage(KafkaGenericOutputDTO outputDTO) {
		kafkaProducerStateConfig.getTemplate().send(outputDTO.getTopic(), outputDTO)
				.addCallback((SendResult<String, KafkaGenericOutputDTO> result) -> {
				}, (Throwable exception) -> {
					if (exception.getCause() instanceof TimeoutException && kafkaProducerStateConfig.getOutputRepository() != null) {
						InactiveStoredMessageState inactiveStoredMessageState = new InactiveStoredMessageState(kafkaProducerStateConfig);
						kafkaProducerStateConfig.getKafkaMessageProducer().changeState(inactiveStoredMessageState);
						inactiveStoredMessageState.sendOutputMessage(outputDTO);
					}
				});

	}

}
