package org.example.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.message.JsonMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
public class Producer {

	private final KafkaProperties kafkaProperties;
	private final KafkaTemplate<String, Object> template;

	private final Random random = new SecureRandom();
	private final AtomicInteger counter = new AtomicInteger();

	@Scheduled(fixedRate = 60000)
	public void send() {
		int batchSize = random.nextInt(10) + 1;
		log.info("Sending {} messages at once", batchSize);
		for (int i = 0; i < batchSize; i++) {
			final int key = counter.getAndIncrement();
			final JsonMessage payload = new JsonMessage(UUID.randomUUID(), random.nextLong());
			log.info("Sending message with key: {}, payload: {}", key, payload);
			template.send("tryout", String.format("%s-%s", kafkaProperties.getProducer().getClientId(), key), payload);
		}
	}

}
