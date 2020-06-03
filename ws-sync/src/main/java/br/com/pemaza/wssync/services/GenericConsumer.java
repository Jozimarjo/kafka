package br.com.pemaza.wssync.services;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.repositories.GenericDatabaseRepository;

@Service
public class GenericConsumer {
    private Logger logger = LoggerFactory.getLogger(GenericConsumer.class);

    @Value("${kafka.consumer.topics}")
    private String[] topics;

    @Value("${kafka.consumer.group}")
    private String listenerGroup;

    @Autowired
    @Qualifier("genericKafkaListener")
    private ConcurrentKafkaListenerContainerFactory<String, KafkaGenericOutputDTO> kafkaConsumerFactory;

    @Autowired
    @Qualifier("taskExecutor")
    TaskExecutor executor;

    @Autowired
    PontoParadaService pontoParadaService;

    @Autowired
    GenericDatabaseRepository repository;

    @KafkaListener(topics = "${kafka.consumer.topics}", containerFactory = "genericKafkaListener")
    public void listenPessoa(KafkaGenericOutputDTO outputDTO) {
        if (differentSource(outputDTO.getSource())) {
            pontoParadaService.add(outputDTO);
            logger.warn(String.format("[%s][%s - %s]", outputDTO.getOperation().toString(), outputDTO.getSchema(),
                    outputDTO.getTable()));

            String result = this.persist(outputDTO) > 0 ? "SUCCESS" : "ERROR";

            logger.warn(String.format("[%s - %s][%s - %s]", outputDTO.getOperation().toString(), result,
                    outputDTO.getSchema(), outputDTO.getTable()));
        }
    }

    private boolean differentSource(String src) {
        if (src == null)
            return true;

        return !src.equals(listenerGroup);
    }

    private int persist(KafkaGenericOutputDTO outputDTO) {
        switch (outputDTO.getOperation()) {
        case INSERT:
        case UPDATE:
            return repository.upsertRow(outputDTO.getSchema(), outputDTO.getTable(), outputDTO.getPrimaryKeys(),
                    outputDTO.getColumns());
        case DELETE:
            return repository.deleteRow(outputDTO.getSchema(), outputDTO.getTable(), outputDTO.getPrimaryKeys());
        default:
            throw new RuntimeException("Operação não existente.");
        }
    }
}