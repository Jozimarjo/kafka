package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

//@Service
//@Profile("!matriz")
//public class CatalogoConsumer {
//	@Autowired
//	KafkaConsumers consumers;
//
//	@Value(value = "${ws.catalogo.sync}")
//	private String urlSyncCatalogo = "";
//
//	@KafkaListener(topics = "${kafka.consumer.topic.catalogo}", containerFactory = "kafkaListenerContainerFactory")
//	public void listenPessoa(KafkaMsgDTO msg) {
//		this.consumers.sendRequest(msg, urlSyncCatalogo);
//	}
//}