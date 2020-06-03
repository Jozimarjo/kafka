package br.com.pemaza.wssync.services.messageProducer;

import java.util.Date;
import java.util.List;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

public class ActiveStoredMessageState extends MessageProducerState {

	public ActiveStoredMessageState(KafkaProducerStateConfig kafkaProducerStateConfig) {
		super(kafkaProducerStateConfig);
	}

	@Override
	public void sendMessage(KafkaMsgDTO msgDTO) {
		msgDTO.setTimestamp(new Date());
		kafkaProducerStateConfig.getMsgRepository().save(msgDTO);

	}

	@Override
	public void sendOutputMessage(KafkaGenericOutputDTO outputDTO) {
		outputDTO.setTimestamp(new Date());
		kafkaProducerStateConfig.getOutputRepository().save(outputDTO);

	}

	public void sendMessageMongo() {
		List<KafkaMsgDTO> msgDTOs = kafkaProducerStateConfig.getMsgRepository().findAllByOrderByTimestamp();
		List<KafkaGenericOutputDTO> outputDTOs = kafkaProducerStateConfig.getOutputRepository().findAllByOrderByTimestamp();

		while (msgDTOs.size() > 0 || outputDTOs.size() > 0) {

			for (KafkaMsgDTO msgDTO : msgDTOs) {

				try {
					kafkaProducerStateConfig.getMsgTemplate().send(msgDTO.getTopic(), msgDTO).completable().join();
					kafkaProducerStateConfig.getMsgRepository().delete(msgDTO);
				} catch (Exception e) {
					kafkaProducerStateConfig.getKafkaMessageProducer().changeState(new InactiveStoredMessageState(kafkaProducerStateConfig));
					return;
				}
			}
			
			for (KafkaGenericOutputDTO outputDTO : outputDTOs) {
				try {
					kafkaProducerStateConfig.getTemplate().send(outputDTO.getTopic(), outputDTO).completable().join();
					kafkaProducerStateConfig.getOutputRepository().delete(outputDTO);
				} catch (Exception e) {
					kafkaProducerStateConfig.getKafkaMessageProducer().changeState(new InactiveStoredMessageState(kafkaProducerStateConfig));
					return;
				}
				
			}
			msgDTOs = kafkaProducerStateConfig.getMsgRepository().findAllByOrderByTimestamp(); // Garantir que nao tem msg
			outputDTOs = kafkaProducerStateConfig.getOutputRepository().findAllByOrderByTimestamp();
		}

		kafkaProducerStateConfig.getKafkaMessageProducer().changeState(
				new ActiveNoMessageState(kafkaProducerStateConfig));
	}

}
