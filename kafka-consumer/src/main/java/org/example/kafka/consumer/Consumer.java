package org.example.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.example.kafka.message.JsonMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
@Component
public class Consumer {

	private final KafkaProperties kafkaProperties;

	private final Random random = new SecureRandom();

	@RetryableTopic(attempts = "4", backoff = @Backoff(multiplier = 3), numPartitions = "${kafka.num.partitions:1}", replicationFactor = "${kafka.default.replication.factor:1}")
	@KafkaListener(topics = "tryout", clientIdPrefix = "json", containerFactory = "kafkaListenerContainerFactory")
	public void listenAsJson(ConsumerRecord<String, JsonMessage> cr, @Payload JsonMessage payload) {
		if (random.nextInt(100) < 15) {
			log.error("{} [JSON] listener received, but randomly failed to process key {}: Type [{}] | Payload: {} | Record: {}",
					kafkaProperties.getConsumer().getClientId(), cr.key(), typeIdHeader(cr.headers()), payload, cr);
			throw new IllegalStateException();
		}
		log.info("{} [JSON] listener received key {}: Type [{}] | Payload: {} | Record: {}", kafkaProperties.getConsumer().getClientId(), cr.key(),
				typeIdHeader(cr.headers()), payload, cr);
	}

	@DltHandler
	public void dlt(ConsumerRecord<String, JsonMessage> cr, @Payload JsonMessage payload) {
		log.info("{} [JSON DLT] listener received key {}: Type [{}] | Payload: {} | Record: {}", kafkaProperties.getConsumer().getClientId(),
				cr.key(), typeIdHeader(cr.headers()), payload, cr);
	}

	private static String typeIdHeader(Headers headers) {
		return StreamSupport.stream(headers.spliterator(), false)
				.filter(header -> header.key().equals("__TypeId__"))
				.findFirst()
				.map(header -> new String(header.value()))
				.orElse("N/A");
	}

	//	@KafkaListener(topics = "tryout", clientIdPrefix = "string", batch = "true", containerFactory = "kafkaListenerStringContainerFactory")
	//	public void listenAsString(List<ConsumerRecord<String, String>> crs) {
	//		log.info("{} [String] received a batch of {} records", kafkaProperties.getConsumer().getClientId(), crs.size());
	//		crs.forEach(cr -> log.info("{} [String] listener received key {}: Type [{}] | Payload: {} | Record: {}",
	//				kafkaProperties.getConsumer().getClientId(), cr.key(), typeIdHeader(cr.headers()), cr.value(), cr));
	//	}
	//
	//	@KafkaListener(topics = "tryout", clientIdPrefix = "bytearray", containerFactory = "kafkaListenerByteArrayContainerFactory")
	//	public void listenAsByteArray(ConsumerRecord<String, byte[]> cr, @Payload byte[] payload) {
	//		log.info("{} [ByteArray] received key {}: Type [{}] | Payload: {} | Record: {}", kafkaProperties.getConsumer().getClientId(), cr.key(),
	//				typeIdHeader(cr.headers()), payload, cr);
	//	}

}
