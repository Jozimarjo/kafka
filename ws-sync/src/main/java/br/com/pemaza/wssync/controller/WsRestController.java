package br.com.pemaza.wssync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.pemaza.wssync.dto.KafkaGenericInputDTO;
import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;
import br.com.pemaza.wssync.dto.KafkaMsgDTO;
import br.com.pemaza.wssync.services.KafkaService;

@RestController
public class WsRestController {

	@Autowired
	KafkaTemplate<String, KafkaMsgDTO> kafkaTemplate;

	@Autowired
	KafkaTemplate<String, KafkaGenericOutputDTO> kafkaGenericTemplate;

	@Autowired
	KafkaService service;

	@Autowired
	@Qualifier("taskExecutor")
	TaskExecutor executor;

	@Value("${kafka.consumer.group}")
	private String listenerGroup;

	@PostMapping(value = "/sendMsg") 
	@ResponseBody
	public void postMethodName(@RequestBody KafkaMsgDTO msgDTO, @RequestParam(name = "topic") String topic,
			@RequestParam(name = "partition") Integer partition, @RequestParam(name = "key") String key) {
		service.sendKafkaMsg(msgDTO, topic);
	}

	@PostMapping(value = "/sendMarketPlace")
	@ResponseBody
	public void postMethodName(@RequestBody KafkaMsgDTO msgDTO, @RequestParam(name = "filial") Integer filial) {
		String topic = "marketplace_".concat(filial.toString());
		service.sendKafkaMsg(msgDTO, topic);
	}

	@PostMapping(value = "/sync") 
	public void sync(@RequestBody KafkaGenericInputDTO inputDTO) {
		service.sendKafkaOutputMsgFromInput(inputDTO);
	}
}
