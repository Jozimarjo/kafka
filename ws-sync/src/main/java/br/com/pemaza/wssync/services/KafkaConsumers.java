package br.com.pemaza.wssync.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Service
public class KafkaConsumers {

	private static int total = 0;
	private static int erro = 0;
	private static final String LOG_MSG_LABEL = "[%s]";
	private static final String LOG_MSG_INFO = "[%s][%s]";
	private static final String LOG_MSG_RESPONSE = "[RESPONSE = %s]";
	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumers.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("taskExecutor")
	TaskExecutor executor;

	@Autowired
	RetryTemplate retryTemplate;

	@Value("${kafka.consumer.group}")
	private String listenerGroup;

	public void sendRequest(KafkaMsgDTO msg, String url) {
		if (differentSource(msg.getSource())) {
			executor.execute(() -> {
				try {
					retryTemplate.execute(context -> {
						logger.warn(String.format(LOG_MSG_LABEL, msg.getLabel()));
						logger.warn(String.format(LOG_MSG_INFO, "Tentativa: ", context.getRetryCount()));
						logger.warn(String.format(LOG_MSG_INFO, "SEND TO: ", url));
						ResponseEntity<String> response = null;
						try {
							response = restTemplate.postForEntity(url, msg.getContent(), String.class);
						} catch (HttpServerErrorException exception) {
							recoverRequest(exception, msg, url);
							return null;
						}
						logger.warn(String.format(LOG_MSG_RESPONSE, response.getStatusCode()));
						total++;
		
						this.showLogs();
						return null;
					});
				} catch (RestClientException e) {
					this.recoverRequest(e, msg, url);
				}
				
			});
		} else {
			logger.info("Preventing msg from same source.");
		}
	}

	private void recoverRequest(RestClientException ex, KafkaMsgDTO msg, String url) {
		logger.warn(String.format(LOG_MSG_INFO, "Exception: ", ex));
		this.showLogs();
	}

	private boolean differentSource(String source) {
		if (source != null)
		return !source.equals(this.listenerGroup);
		return true;
	}

	private void showLogs() {
		logger.warn(String.format(LOG_MSG_INFO, "TOTAL: ", total));
		logger.warn(String.format(LOG_MSG_INFO, "ERROS: ", erro));
	}
}
