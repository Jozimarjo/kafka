package br.com.pemaza.wssync.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaDbInputDTO;
import br.com.pemaza.wssync.dto.KafkaGenericInputDTO;
import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;
import br.com.pemaza.wssync.dto.TypedColumn;
import br.com.pemaza.wssync.repositories.GenericDatabaseRepository;
import br.com.pemaza.wssync.services.messageProducer.KafkaMessageProducer;
import br.com.pemaza.wssync.utils.DbOperationEnum;

@Service
public class KafkaService {
	private final long MIN_5 = 30000;

	@Autowired
	KafkaMessageProducer kafkaMessageProducer;

	@Autowired
	GenericDatabaseRepository genericDatabaseRepository;

	@Autowired
	@Qualifier("taskExecutor")
	TaskExecutor executor;

	@Autowired
	PontoParadaService pontoParadaService;

	@Value("${kafka.consumer.group}")
	private String listenerGroup;

	@Value("${kafka.consumer.address}")
	private String adres;
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	@Value("${kafka.consumer.topic.venda}")
	private String consumer;
	

	@Scheduled(fixedDelay = MIN_5)
	private void setUp() {
		System.out.println("adress: " + adres);
		System.out.println("profile: " + profile);
		System.out.println("consumer: " + consumer);


		List<KafkaDbInputDTO> msg = genericDatabaseRepository.getMsgToSend();
		for (KafkaDbInputDTO msgSync : msg) {
			ObjectMapper obj = new ObjectMapper();
			KafkaGenericInputDTO genericInputDTO = new KafkaGenericInputDTO();
			try {
				genericInputDTO = obj.readValue(msgSync.getReg_dados(), KafkaGenericInputDTO.class);
				sendKafkaOutputMsgFromInput(genericInputDTO);
				changeStatusMsg(msgSync.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void changeStatusMsg(Long id) {
		genericDatabaseRepository.update(id);
	}

	private List<KafkaGenericOutputDTO> inputToOutput(KafkaGenericInputDTO inputDTO) {
		List<TypedColumn> columns = new ArrayList<>();
		if (inputDTO.getOperation() != DbOperationEnum.DELETE) {
			columns = genericDatabaseRepository.getTypedColumnsByTable(inputDTO.getSchema(), inputDTO.getTable(),
					inputDTO.getSyncColumns(), inputDTO.getIgnoreColumns());
		}

		List<TypedColumn> allColumns = genericDatabaseRepository.getAllColumnsUsingPk(inputDTO.getSchema(),
				inputDTO.getTable(), inputDTO.getPrimaryKeys(), columns);
		return inputDTO.getTopics().stream().map(topic -> {
			KafkaGenericOutputDTO outputDTO = KafkaGenericOutputDTO.fromInput(inputDTO);
			outputDTO.setTopic(topic);
			outputDTO.setColumns(allColumns);
			outputDTO.setSource(listenerGroup);
			return outputDTO;
		}).collect(Collectors.toList());
	}

	private Boolean checkRetornoMsg(KafkaGenericOutputDTO outputDTO) {
		int hc = outputDTO.hashCode();
		if (pontoParadaService.containsKey(outputDTO.hashCode())) {
			pontoParadaService.remove(outputDTO);
			return true;
		}
		return false;
	}

	public void sendKafkaOutputMsgFromInput(KafkaGenericInputDTO inputDTO) {
		executor.execute(() -> {
			List<KafkaGenericOutputDTO> outputDTOs = inputToOutput(inputDTO);
			outputDTOs.forEach(outputDTO -> {
				if (!checkRetornoMsg(outputDTO))
					kafkaMessageProducer.sendOutputMessage(outputDTO);
			});
		});

	}

	public void sendKafkaMsg(KafkaMsgDTO msgDTO, String topic) {
		executor.execute(() -> {
			msgDTO.setSource(listenerGroup);
			msgDTO.setTopic(topic);
			kafkaMessageProducer.sendMessage(msgDTO);
		});

	}
}