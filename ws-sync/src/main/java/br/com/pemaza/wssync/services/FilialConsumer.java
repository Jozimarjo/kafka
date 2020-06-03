package br.com.pemaza.wssync.services;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile(value = "filial")
@Service
public class FilialConsumer {
    @Autowired
    KafkaConsumers consumers;

    @Value("${ws.b2b.pedido.sync}")
    String urlSyncB2BPedido = "";

    @KafkaListener(topics = "${kafka.consumer.filial}", containerFactory = "kafkaListenerContainerFactory")
    public void listenVenda(KafkaMsgDTO msg) {
        this.consumers.sendRequest(msg, urlSyncB2BPedido);
    }
}
