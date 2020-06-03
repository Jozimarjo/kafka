package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Profile(value = "matriz")
@Service
public class VendaConsumer {
	@Autowired
	KafkaConsumers consumers;

	@Value(value = "${ws.pedido.sync}")
	private String urlSyncPedido = "";

	@KafkaListener(topics = "${kafka.consumer.topic.venda}", containerFactory = "kafkaListenerContainerFactory")
	public void listenVenda(KafkaMsgDTO msg) {
		this.consumers.sendRequest(msg, urlSyncPedido);
	}
}