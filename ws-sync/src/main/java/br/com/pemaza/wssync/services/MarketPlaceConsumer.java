package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Profile(value = "filial")
@Service
public class MarketPlaceConsumer {
	@Autowired
	KafkaConsumers consumers;

	@Value("${ws.marketplace.sync}")
	String urlSyncPedido = "";

	@KafkaListener(topics = "${kafka.consumer.topic.marketplace}", containerFactory = "kafkaListenerContainerFactory")
	public void listenVenda(KafkaMsgDTO msg) {
		this.consumers.sendRequest(msg, urlSyncPedido);
	}
}
