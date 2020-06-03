package br.com.pemaza.wssync.services.messageProducer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;

public class InactiveStoredMessageState extends MessageProducerState {

	public InactiveStoredMessageState(KafkaProducerStateConfig kafkaProducerStateConfig) {
		super(kafkaProducerStateConfig);
		checkKafkaConnection();
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

	public void checkKafkaConnection() {
		final Map<String, Object> configs = new HashMap<>();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerStateConfig.getBootstrapServer());
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProducerStateConfig.getListenerGroup());
		configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
		configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		Thread thread = new Thread(() -> {
			boolean conexaoAtiva = false;
			while (!conexaoAtiva) {
				try {
					KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
					consumer.listTopics();
					consumer.close();
					conexaoAtiva = true;
					ActiveStoredMessageState asms = new ActiveStoredMessageState(kafkaProducerStateConfig);
					kafkaProducerStateConfig.getKafkaMessageProducer().changeState(asms);
					asms.sendMessageMongo();

				} catch (Exception e) {
					try {
						Thread.sleep(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		thread.start();
	}

}
