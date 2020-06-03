package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Profile("!matriz")
@Service
public class ControleAcessoConsumer {
    @Autowired
    KafkaConsumers consumers;

    @Value(value = "${ws.controle.acesso.sync}")
	private String urlSyncUsuario;

	@KafkaListener(topics = "${kafka.consumer.topic.controle-acesso}", containerFactory = "kafkaListenerContainerFactory")
	public void listenUsuario(KafkaMsgDTO msg) {
		this.consumers.sendRequest(msg, urlSyncUsuario);
	}
}