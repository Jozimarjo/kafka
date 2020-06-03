package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Service
@Profile("!b2b")
public class PessoaConsumer {
    @Autowired
    KafkaConsumers consumers;

    @Value(value = "${ws.pessoa.sync}")
	private String urlSyncPessoa = "";

    @KafkaListener(topics = "${kafka.consumer.topic.pessoa}", containerFactory = "kafkaListenerContainerFactory")
	public void listenPessoa(KafkaMsgDTO msg) {
		this.consumers.sendRequest(msg, urlSyncPessoa);
	}
}