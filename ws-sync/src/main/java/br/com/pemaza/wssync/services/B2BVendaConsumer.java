package br.com.pemaza.wssync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Profile(value = "matriz")
@Service
public class B2BVendaConsumer {
    @Autowired
    KafkaConsumers consumers;

    @Value("${ws.b2b.pedido.sync}")
    String urlSyncB2BPedido = "";

    @KafkaListener(topics = "${kafka.consumer.topic.b2b.venda}", containerFactory = "kafkaListenerContainerFactory")
	public void listenVenda(KafkaMsgDTO msg) {
		this.consumers.sendRequest(msg, urlSyncB2BPedido);
	}
}