package org.example.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ConsumerConfiguration {

	private final KafkaProperties kafkaProperties;

	// Not needed if @RetryableTopic autocreates the topic
	// Generally it will override (recreate? extend?) the topic with numPartitions and replicationFactor set on the annotation even if it is created here.
	//	@Bean
	//	public NewTopic tryout() {
	//		return TopicBuilder.name("tryout").partitions(15).replicas(3).compact().build();
	//	}

	@Bean
	public Map<String, Object> jsonProducerConfigs() {
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return props;
	}

	@Bean
	public ProducerFactory<String, Object> jsonProducerFactory() {
		return new DefaultKafkaProducerFactory<>(jsonProducerConfigs());
	}

	@Bean
	public KafkaTemplate<String, Object> retryTopicDefaultKafkaTemplate() {
		return new KafkaTemplate<>(jsonProducerFactory());
	}

	@Bean
	public ConsumerFactory<String, Object> consumerFactory() {
		final JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
		jsonDeserializer.addTrustedPackages("*");
		return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), jsonDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}

	//	@Bean
	//	public ConsumerFactory<String, String> stringConsumerFactory() {
	//		return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
	//	}
	//
	//	@Bean
	//	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerStringContainerFactory() {
	//		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
	//		factory.setConsumerFactory(stringConsumerFactory());
	//
	//		return factory;
	//	}
	//
	//	@Bean
	//	public ConsumerFactory<String, byte[]> byteArrayConsumerFactory() {
	//		return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new ByteArrayDeserializer());
	//	}
	//
	//	@Bean
	//	public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerByteArrayContainerFactory() {
	//		ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
	//		factory.setConsumerFactory(byteArrayConsumerFactory());
	//		return factory;
	//	}

}
